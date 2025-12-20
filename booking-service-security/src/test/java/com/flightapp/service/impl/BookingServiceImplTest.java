package com.flightapp.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.dto.FlightDto;
import com.flightapp.feign.FlightClient;
import com.flightapp.messaging.BookingEvent;
import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.repository.TicketRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BookingServiceImplTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private PassengerRepository passengerRepository;

	@Mock
	private FlightClient flightClient;

	@Mock
	private KafkaTemplate<String, BookingEvent> kafkaTemplate;

	@InjectMocks
	private BookingServiceImpl bookingService;

	private Passenger passenger;
	private FlightDto depFlight;
	private final String token = "Bearer dummy-token";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		passenger = new Passenger();
		passenger.setName("Poojith");
		passenger.setAge(30);
		passenger.setGender("Male");
		passenger.setSeatNumber("A1");

		depFlight = new FlightDto();
		depFlight.setId("FL1");
		depFlight.setAvailableSeats(5);
		depFlight.setPrice(100.0);
	}

//	@Test
//	void testBookTicketSuccess() {
//		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
//		when(flightClient.reserveSeats("FL1", 1, token)).thenReturn("OK");
//
//		when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
//		when(passengerRepository.saveAll(anyList())).thenReturn(Flux.fromIterable(List.of(passenger)));
//
//		StepVerifier
//				.create(bookingService.bookTicket("pooja@gmail.com", "FL1", null, List.of(passenger),
//						FLIGHTTYPE.ONE_WAY, token))
//				.expectNextMatches(pnr -> pnr != null && !pnr.isEmpty()).verifyComplete();
//
//		verify(flightClient).reserveSeats("FL1", 1, token);
//	}

//	@Test
//	void testBookTicketNotEnoughSeats() {
//		depFlight.setAvailableSeats(0);
//		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
//
//		StepVerifier.create(bookingService.bookTicket("pooja@gmail.com", "FL1", null, List.of(passenger),
//				FLIGHTTYPE.ONE_WAY, token)).expectError(ResponseStatusException.class).verify();
//	}

	@Test
	void testGetByPnr() {
		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));

		StepVerifier.create(bookingService.getByPnr("PNR123")).expectNext(ticket).verifyComplete();
	}

//	@Test
//	void testHistoryByEmail() {
//		Ticket ticket = new Ticket();
//		ticket.setPnr("PNR123");
//		when(ticketRepository.findByUserEmail("pooja@gmail.com")).thenReturn(Flux.just(ticket));
//
//		StepVerifier.create(bookingService.historyByEmail("pooja@gmail.com")).expectNext(ticket).verifyComplete();
//	}

	@Test
	void testCancelByPnrSuccess() {
		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		ticket.setDepartureFlightId("FL1");
		ticket.setSeatsBooked("A1");
		ticket.setCanceled(false);

		depFlight.setDepartureTime(LocalDateTime.now().plusDays(2));

		when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));
		when(ticketRepository.save(any(Ticket.class))).thenReturn(Mono.just(ticket));
		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
		when(flightClient.releaseSeats("FL1", 1, token)).thenReturn("OK");

		StepVerifier.create(bookingService.cancelByPnr("PNR123", token)).expectNext("Cancelled Successfully")
				.verifyComplete();

		verify(flightClient).releaseSeats("FL1", 1, token);
		verify(ticketRepository).save(any(Ticket.class));
	}

	@Test
	void testCancelByPnrAlreadyCancelled() {
		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		ticket.setCanceled(true);

		when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));

		StepVerifier.create(bookingService.cancelByPnr("PNR123", token)).expectNext("Ticket already cancelled")
				.verifyComplete();
	}

	@Test
	void testCancelByPnrNotFound() {
		when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.empty());

		StepVerifier.create(bookingService.cancelByPnr("PNR123", token))
				.expectErrorMatches(e -> e instanceof ResponseStatusException
						&& ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
				.verify();
	}

//	@Test
//	void testBookTicketRoundTripReturnFlightNotFound() {
//		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
//		when(flightClient.getFlight("FL2", token)).thenReturn(null);
//
//		StepVerifier
//				.create(bookingService.bookTicket("pooja@gmail.com", "FL1", "FL2", List.of(passenger),
//						FLIGHTTYPE.ROUND_TRIP, token))
//				.expectErrorMatches(e -> e instanceof ResponseStatusException
//						&& ((ResponseStatusException) e).getReason().equals("Return flight not found"))
//				.verify();
//	}

//	@Test
//	void testBookTicketReturnFlightNotEnoughSeats() {
//		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
//		FlightDto retFlight = new FlightDto();
//		retFlight.setId("FL2");
//		retFlight.setAvailableSeats(0);
//		retFlight.setPrice(100.0);
//
//		when(flightClient.getFlight("FL2", token)).thenReturn(retFlight);
//
//		StepVerifier.create(bookingService.bookTicket("pooja@gmail.com", "FL1", "FL2", List.of(passenger),
//				FLIGHTTYPE.ROUND_TRIP, token)).expectError(ResponseStatusException.class).verify();
//	}
//
//	@Test
//	void testCreateTicketAddsReturnFlightPrice() {
//		FlightDto retFlight = new FlightDto();
//		retFlight.setId("FL2");
//		retFlight.setPrice(150.0);
//		retFlight.setAvailableSeats(5);
//
//		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
//		when(flightClient.getFlight("FL2", token)).thenReturn(retFlight);
//
//		when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
//		when(passengerRepository.saveAll(anyList())).thenReturn(Flux.fromIterable(List.of(passenger)));
//
//		StepVerifier.create(bookingService.bookTicket("pooja@gmail.com", "FL1", "FL2", List.of(passenger),
//				FLIGHTTYPE.ROUND_TRIP, token)).expectNextCount(1).verifyComplete();
//
//		verify(ticketRepository)
//				.save(argThat(ticket -> ticket.getTotalPrice() == (depFlight.getPrice() + retFlight.getPrice())));
//	}

	@Test
	void testCancelByPnrCalculatesSeatCount() {
		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		ticket.setDepartureFlightId("FL1");
		ticket.setReturnFlightId("FL2");
		ticket.setSeatsBooked("A1,A2");
		ticket.setCanceled(false);

		depFlight.setDepartureTime(LocalDateTime.now().plusDays(2));

		when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));

		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);
		when(flightClient.getFlight("FL2", token)).thenReturn(depFlight);

		when(flightClient.releaseSeats(eq("FL1"), eq(2), eq(token))).thenReturn("OK");
		when(flightClient.releaseSeats(eq("FL2"), eq(2), eq(token))).thenReturn("OK");

		when(ticketRepository.save(any(Ticket.class))).thenReturn(Mono.just(ticket));

		StepVerifier.create(bookingService.cancelByPnr("PNR123", token)).expectNext("Cancelled Successfully")
				.verifyComplete();

		verify(flightClient).releaseSeats("FL1", 2, token);
		verify(flightClient).releaseSeats("FL2", 2, token);
	}

	@Test
	void testCancelByPnrNoReturnFlight() {

		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		ticket.setDepartureFlightId("FL1");
		ticket.setReturnFlightId(null);
		ticket.setSeatsBooked("A1");
		ticket.setCanceled(false);

		depFlight = new FlightDto();
		depFlight.setId("FL1");
		depFlight.setDepartureTime(LocalDateTime.now().plusDays(2));

		when(ticketRepository.findByPnr("PNR123")).thenReturn(Mono.just(ticket));

		when(ticketRepository.save(any(Ticket.class))).thenReturn(Mono.just(ticket));

		when(flightClient.getFlight("FL1", token)).thenReturn(depFlight);

		when(flightClient.releaseSeats("FL1", 1, token)).thenReturn("OK");

		StepVerifier.create(bookingService.cancelByPnr("PNR123", token)).expectNext("Cancelled Successfully")
				.verifyComplete();

		verify(flightClient).releaseSeats("FL1", 1, token);
		verify(flightClient, never()).releaseSeats(eq("FL2"), anyInt(), eq(token));
	}
}
