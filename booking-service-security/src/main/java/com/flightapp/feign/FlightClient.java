package com.flightapp.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.flightapp.dto.FlightDto;

@FeignClient(name = "flight-service")
public interface FlightClient {

	@GetMapping("/api/flight/{id}")
	FlightDto getFlight(@PathVariable String id);

	@PutMapping("/api/flight/internal/{id}/reserve/{seatCount}")
	Object reserveSeats(@PathVariable String id, @PathVariable int seatCount);

	@PutMapping("/api/flight/internal/{id}/release/{seatCount}")
	Object releaseSeats(@PathVariable String id, @PathVariable int seatCount);
}