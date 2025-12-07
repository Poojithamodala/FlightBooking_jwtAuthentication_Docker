package com.flightapp.service;

import com.flightapp.model.FLIGHTTYPE;
import com.flightapp.model.Passenger;
import com.flightapp.model.Ticket;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BookingService {

	Mono<String> bookTicket(String userEmail, String departureFlightId, String returnFlightId,
			List<Passenger> passengers, FLIGHTTYPE tripType);

	Mono<Ticket> getByPnr(String pnr);

	Flux<Ticket> historyByEmail(String email);

	Mono<String> cancelByPnr(String pnr);
}