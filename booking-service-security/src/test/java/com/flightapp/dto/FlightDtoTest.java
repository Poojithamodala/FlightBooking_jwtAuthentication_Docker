package com.flightapp.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FlightDtoTest {

	private FlightDto flight;

	@BeforeEach
	void setUp() {
		flight = new FlightDto();
	}

	@Test
	void testGettersAndSetters() {
		String id = "FL123";
		String flightNumber = "AI202";
		String fromPlace = "Hyderabad";
		String toPlace = "Delhi";
		LocalDateTime departureTime = LocalDateTime.of(2025, 12, 1, 10, 30);
		LocalDateTime arrivalTime = LocalDateTime.of(2025, 12, 1, 13, 30);
		double price = 4500.50;
		int availableSeats = 100;

		flight.setId(id);
		flight.setFlightNumber(flightNumber);
		flight.setFromPlace(fromPlace);
		flight.setToPlace(toPlace);
		flight.setDepartureTime(departureTime);
		flight.setArrivalTime(arrivalTime);
		flight.setPrice(price);
		flight.setAvailableSeats(availableSeats);

		assertEquals(id, flight.getId());
		assertEquals(flightNumber, flight.getFlightNumber());
		assertEquals(fromPlace, flight.getFromPlace());
		assertEquals(toPlace, flight.getToPlace());
		assertEquals(departureTime, flight.getDepartureTime());
		assertEquals(arrivalTime, flight.getArrivalTime());
		assertEquals(price, flight.getPrice());
		assertEquals(availableSeats, flight.getAvailableSeats());
	}
}
