package com.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.controller.FlightController;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.model.Flight;
import com.flightapp.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class FlightControllerTest {

	@Mock
	private FlightService flightService;

	private FlightController flightController;

	@BeforeEach
	void setUp() {
		flightController = new FlightController(flightService);
	}

	@Test
	void addInventory_shouldReturnFlightIdAndMessage() {

		// Arrange
		Flight flight = new Flight();
		flight.setAirline("Indigo");
		flight.setFromPlace("BLR");
		flight.setToPlace("HYD");
		flight.setDepartureTime(LocalDateTime.parse("2025-12-01T10:00"));
		flight.setArrivalTime(LocalDateTime.parse("2025-12-01T11:30"));
		flight.setTotalSeats(100);
		flight.setPrice(2500);

		Flight saved = new Flight();
		saved.setId("flight-123");

		when(flightService.addFlight(any(Flight.class))).thenReturn(Mono.just(saved));

		// Act
		Mono<Map<String, String>> result = flightController.addInventory(flight);

		// Assert
		StepVerifier.create(result).assertNext(map -> {
			assertThat(map).containsEntry("message", "Flight added successfully").containsEntry("flightId",
					"flight-123");
		}).verifyComplete();

		// Capture request argument
		ArgumentCaptor<Flight> captor = ArgumentCaptor.forClass(Flight.class);
		verify(flightService, times(1)).addFlight(captor.capture());

		assertThat(captor.getValue()).isSameAs(flight);
	}

	@Test
	void searchFlights_shouldCallService() {
		LocalDateTime start = LocalDateTime.parse("2025-12-01T10:00");
		LocalDateTime end = LocalDateTime.parse("2025-12-01T20:00");

		FlightSearchRequest request = new FlightSearchRequest();
		request.setFromPlace("BLR");
		request.setToPlace("DEL");
		request.setStartTime(start);
		request.setEndTime(end);

		Flight f1 = new Flight();
		f1.setId("f1");
		Flight f2 = new Flight();
		f2.setId("f2");

		when(flightService.searchFlights("BLR", "DEL", start, end)).thenReturn(Flux.just(f1, f2));

		StepVerifier.create(flightController.searchFlights(request)).expectNext(f1).expectNext(f2).verifyComplete();

		verify(flightService).searchFlights("BLR", "DEL", start, end);
	}

	@Test
	void searchByAirline_shouldDelegateToService() {
		Flight f = new Flight();
		f.setId("f1");

		when(flightService.searchFlightsByAirline("BLR", "DEL", "Indigo")).thenReturn(Flux.just(f));

		Map<String, String> body = Map.of("fromPlace", "BLR", "toPlace", "DEL", "airline", "Indigo");

		StepVerifier.create(flightController.searchByAirline(body)).expectNext(f).verifyComplete();

		verify(flightService).searchFlightsByAirline("BLR", "DEL", "Indigo");
	}

	@Test
	void getFlightById_shouldCallService() {
		Flight flight = new Flight();
		flight.setId("f1");

		when(flightService.searchFlightById("f1")).thenReturn(Mono.just(flight));

		StepVerifier.create(flightController.getFlightById("f1")).expectNext(flight).verifyComplete();

		verify(flightService).searchFlightById("f1");
	}

	@Test
	void reserveSeats_shouldCallService() {
		Flight f = new Flight();
		f.setId("f1");

		when(flightService.reserveSeats("f1", 2)).thenReturn(Mono.just(f));

		StepVerifier.create(flightController.reserveSeats("f1", 2)).expectNext(f).verifyComplete();

		verify(flightService).reserveSeats("f1", 2);
	}

	@Test
	void releaseSeats_shouldCallService() {
		Flight f = new Flight();
		f.setId("f1");

		when(flightService.releaseSeats("f1", 2)).thenReturn(Mono.just(f));

		StepVerifier.create(flightController.releaseSeats("f1", 2)).expectNext(f).verifyComplete();

		verify(flightService).releaseSeats("f1", 2);
	}
}
