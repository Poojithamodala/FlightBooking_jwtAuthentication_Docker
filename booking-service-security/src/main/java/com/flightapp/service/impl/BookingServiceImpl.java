package com.flightapp.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.dto.FlightDto;
import com.flightapp.exception.FlightBookingException;
import com.flightapp.feign.FlightClient;
import com.flightapp.feign.FlightWebClient;
import com.flightapp.messaging.BookingEvent;
import com.flightapp.model.BookingHistoryResponse;
import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.repository.TicketRepository;
import com.flightapp.service.BookingService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	private final TicketRepository ticketRepository;
	private final PassengerRepository passengerRepository;
	private final FlightClient flightClient;
	private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
	private final FlightWebClient flightWebClient;

	private static final String TOPIC = "booking-events";

	@Value("${spring.security.oauth2.resourceserver.jwt.secret}")
	private String secret;

	private SecretKey key;

	@PostConstruct
	public void init() {
		key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	private String extractEmail(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token.replace("Bearer ", "")).getBody()
				.getSubject();
	}
	
	@Override
	public Mono<String> bookTicket(String departureFlightId, String returnFlightId, List<Passenger> passengers,
			FLIGHTTYPE tripType, String token) {
		String userEmail = extractEmail(token);
		int seatCount = passengers.size();

		if (passengers == null || passengers.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passengers list cannot be empty");
		}

		if (tripType == FLIGHTTYPE.ROUND_TRIP && returnFlightId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Return flight is required for round trip");
		}

		Set<String> seats = passengers.stream().map(Passenger::getSeatNumber).collect(Collectors.toSet());

		if (seats.size() != passengers.size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate seat numbers are not allowed");
		}

		Mono<Void> seatAvailabilityCheck = Flux.fromIterable(passengers).flatMap(p -> passengerRepository
				.existsByFlightIdAndSeatNumber(departureFlightId, p.getSeatNumber()).flatMap(exists -> {
					if (exists) {
						return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
								"Seat " + p.getSeatNumber() + " is already booked"));
					}
					return Mono.empty();
				})).then();

		return seatAvailabilityCheck.then(Mono.fromCallable(() -> {
			FlightDto depFlight = getFlightOrThrow(departureFlightId, seatCount, "Departure", token);

			FlightDto retFlight = getReturnFlightIfNeeded(returnFlightId, tripType, seatCount, token);

			reserveFlights(retFlight, departureFlightId, returnFlightId, seatCount, token);

			return new CheckedFlights(depFlight, retFlight);
		}).subscribeOn(Schedulers.boundedElastic()))
				.flatMap(checked -> createTicket(userEmail, departureFlightId, returnFlightId, passengers, tripType,
						checked.dep(), checked.ret()))
				.onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e)));
	}

	private FlightDto getFlightOrThrow(String flightId, int seatCount, String type, String token) {
		FlightDto flight = flightClient.getFlight(flightId, token);
		if (flight == null)
			throw new FlightBookingException(type + " flight not found");
		if (flight.getAvailableSeats() < seatCount)
			throw new FlightBookingException("Not enough seats in " + type.toLowerCase() + " flight");
		return flight;
	}

	private FlightDto getReturnFlightIfNeeded(String returnFlightId, FLIGHTTYPE tripType, int seatCount, String token) {
		if (tripType == FLIGHTTYPE.ROUND_TRIP && returnFlightId != null) {
			return getFlightOrThrow(returnFlightId, seatCount, "Return", token);
		}
		return null;
	}

	private void reserveFlights(FlightDto retFlight, String departureFlightId, String returnFlightId, int seatCount,
			String token) {

		flightClient.reserveSeats(departureFlightId, seatCount, token);

		if (retFlight != null) {
			try {
				flightClient.reserveSeats(returnFlightId, seatCount, token);
			} catch (Exception e) {
				flightClient.releaseSeats(departureFlightId, seatCount, token);
				throw new FlightBookingException("Failed to reserve return flight, rolled back departure", e);
			}
		}
	}

	private Mono<String> createTicket(String userEmail, String departureFlightId, String returnFlightId,
			List<Passenger> passengers, FLIGHTTYPE tripType, FlightDto depFlight, FlightDto retFlight) {

		Ticket ticket = new Ticket();
		ticket.setPnr(UUID.randomUUID().toString().substring(0, 8));
		ticket.setUserEmail(userEmail);
		ticket.setDepartureFlightId(departureFlightId);
		ticket.setReturnFlightId(returnFlightId);
		ticket.setTripType(tripType);
		ticket.setBookingTime(LocalDateTime.now());
		ticket.setSeatsBooked(passengers.stream().map(Passenger::getSeatNumber).collect(Collectors.joining(",")));

		int seatCount = passengers.size();
		double total = depFlight.getPrice() * seatCount;
		if (retFlight != null) {
			total += retFlight.getPrice() * seatCount;
		}
		ticket.setTotalPrice(total);
		ticket.setCanceled(false);
		
		
		return ticketRepository.save(ticket).flatMap(saved -> {
			passengers.forEach(p -> p.setTicketId(saved.getId()));
			return passengerRepository.saveAll(passengers).then(Mono.just(saved));
		}).doOnSuccess(saved -> sendEvent("BOOKING_CONFIRMED", saved)).map(Ticket::getPnr);
	}

	@Override
	public Mono<Ticket> getByPnr(String pnr) {
		return ticketRepository.findByPnr(pnr);
	}

//	@Override
//	public Flux<Ticket> history(Authentication authentication) {
//
//	    JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
//	    Jwt jwt = jwtAuth.getToken();
//	    String email = jwt.getSubject();
//	    return ticketRepository.findByUserEmail(email);
//	}
	@Override
	public Flux<BookingHistoryResponse> historyByEmail(String email, String token) {

	    System.out.println("📩 Fetching booking history for: " + email);

	    String authHeader = token.startsWith("Bearer ")
	            ? token
	            : "Bearer " + token;

	    return ticketRepository.findByUserEmail(email)
	            .flatMap(ticket ->
	                    Mono.zip(
	                            flightWebClient.getFlight(
	                                    ticket.getDepartureFlightId(),
	                                    authHeader
	                            ),
	                            passengerRepository
	                                    .findByTicketId(ticket.getId())
	                                    .collectList()
	                    )
	                    .map(tuple -> {
	                        FlightDto flight = tuple.getT1();
	                        List<Passenger> passengers = tuple.getT2();

	                        return new BookingHistoryResponse(
	                                ticket.getId(),
	                                ticket.getPnr(),
	                                ticket.getTripType(),
	                                ticket.getBookingTime(),
	                                ticket.getSeatsBooked(),
	                                ticket.getMealType(),
	                                ticket.getTotalPrice(),
	                                ticket.isCanceled(),

	                                // flight details
	                                flight.getAirline(),
	                                flight.getFromPlace(),
	                                flight.getToPlace(),
	                                flight.getDepartureTime(),
	                                flight.getArrivalTime(),

	                                // passengers
	                                passengers
	                        );
	                    })
	            )
	            .doOnNext(r ->
	                    System.out.println("✅ History record created for PNR: " + r.getPnr())
	            )
	            .doOnError(e ->
	                    System.err.println("❌ History error: " + e.getMessage())
	            );
	}

	@Override
	public Mono<String> cancelByPnr(String pnr, String token) {

		return ticketRepository.findByPnr(pnr)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "PNR not found")))
				.flatMap(ticket -> {

					if (ticket.isCanceled()) {
						return Mono.just("Ticket already cancelled");
					}

					int seatCount = (ticket.getSeatsBooked() != null && !ticket.getSeatsBooked().isEmpty())
							? ticket.getSeatsBooked().split(",").length
							: 1;

					Mono<FlightDto> depFlightMono = Mono.fromCallable(() -> {
						FlightDto depFlight = flightClient.getFlight(ticket.getDepartureFlightId(), token);
						if (depFlight == null)
							throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Departure flight not found");

						LocalDateTime now = LocalDateTime.now();
						if (depFlight.getDepartureTime().minusHours(24).isBefore(now)) {
							throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
									"Cannot cancel ticket within 24 hours of departure");
						}
						return depFlight;
					}).subscribeOn(Schedulers.boundedElastic());

					Mono<Void> releaseSeatsMono = Mono.fromRunnable(() -> {

						flightClient.releaseSeats(ticket.getDepartureFlightId(), seatCount, token);

						if (ticket.getReturnFlightId() != null) {
							flightClient.releaseSeats(ticket.getReturnFlightId(), seatCount, token);
						}
					}).subscribeOn(Schedulers.boundedElastic()).then();

					Mono<String> updateCancelMono = updateCancellation(ticket);

					return depFlightMono.then(releaseSeatsMono).then(updateCancelMono);
				});
	}

	private Mono<String> updateCancellation(Ticket ticket) {
		ticket.setCanceled(true);
		return ticketRepository.save(ticket).doOnSuccess(saved -> sendEvent("BOOKING_CANCELLED", saved))
				.thenReturn("Cancelled Successfully");
	}

	private void sendEvent(String eventType, Ticket ticket) {
		BookingEvent event = BookingEvent.builder().eventType(eventType).pnr(ticket.getPnr())
				.userEmail(ticket.getUserEmail()).totalPrice(ticket.getTotalPrice()).build();
		try {
			kafkaTemplate.send(TOPIC, ticket.getPnr(), event);
		} catch (Exception ex) {
			// log
		}
	}

	private record CheckedFlights(FlightDto dep, FlightDto ret) {
	}
}