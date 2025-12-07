package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;
import com.flightapp.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class BookingControllerTest {

	private BookingService bookingService;
	private BookingController bookingController;

	@BeforeEach
	void setUp() {
		bookingService = Mockito.mock(BookingService.class);
		bookingController = new BookingController(bookingService);
	}

	@Test
	void testBookTicket() {
		BookingController.BookingRequest request = new BookingController.BookingRequest();
		request.setUserEmail("pooja@gmail.com");
		request.setReturnFlightId("RET123");
		request.setTripType(FLIGHTTYPE.ROUND_TRIP);

		Passenger passenger = new Passenger();
		passenger.setName("Poojith");
		passenger.setAge(30);
		passenger.setGender("Male");
		passenger.setSeatNumber("A1");
		request.setPassengers(List.of(passenger));

		when(bookingService.bookTicket(anyString(), anyString(), anyString(), anyList(), any(FLIGHTTYPE.class)))
				.thenReturn(Mono.just("PNR123"));

		StepVerifier.create(bookingController.bookTicket("DEP123", request)).expectNext("PNR123").verifyComplete();

		verify(bookingService).bookTicket("pooja@gmail.com", "DEP123", "RET123", request.getPassengers(),
				FLIGHTTYPE.ROUND_TRIP);
	}

	@Test
	void testGetTicket() {
		Ticket ticket = new Ticket();
		ticket.setPnr("PNR123");
		ticket.setUserEmail("pooja@gmail.com");
		ticket.setDepartureFlightId("DEP123");
		ticket.setTripType(FLIGHTTYPE.ONE_WAY);
		ticket.setBookingTime(LocalDateTime.now());

		when(bookingService.getByPnr("PNR123")).thenReturn(Mono.just(ticket));

		StepVerifier.create(bookingController.getTicket("PNR123")).expectNext(ticket).verifyComplete();

		verify(bookingService).getByPnr("PNR123");
	}

	@Test
	void testHistory() {
		Ticket ticket1 = new Ticket();
		ticket1.setPnr("PNR1");
		Ticket ticket2 = new Ticket();
		ticket2.setPnr("PNR2");

		when(bookingService.historyByEmail("pooja@gmail.com")).thenReturn(Flux.just(ticket1, ticket2));

		StepVerifier.create(bookingController.history("pooja@gmail.com")).expectNext(ticket1).expectNext(ticket2)
				.verifyComplete();

		verify(bookingService).historyByEmail("pooja@gmail.com");
	}

	@Test
	void testCancel() {
		when(bookingService.cancelByPnr("PNR123")).thenReturn(Mono.just("Cancelled"));

		StepVerifier.create(bookingController.cancel("PNR123")).expectNext("Cancelled").verifyComplete();

		verify(bookingService).cancelByPnr("PNR123");
	}
}
