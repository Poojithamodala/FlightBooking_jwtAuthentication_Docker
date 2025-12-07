package com.flightapp.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document(collection = "flights")
public class Flight {

	@Id
	private String id;

	@NotBlank(message = "Airline name cannot be null")
	private String airline;

	@NotBlank(message = "From place cannot be null")
	private String fromPlace;

	@NotBlank(message = "To place cannot be null")
	private String toPlace;

	@NotNull(message = "Departure time cannot be null")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
	private LocalDateTime departureTime;

	@NotNull(message = "Arrival time cannot be null")
	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
	private LocalDateTime arrivalTime;

	@Min(value = 1, message = "Price should be at least 1")
	private int price;

	@Min(value = 1, message = "Total seats should be at least 1")
	private int totalSeats;

	@Min(value = 0, message = "Available seats cannot be negative")
	private int availableSeats;
}
