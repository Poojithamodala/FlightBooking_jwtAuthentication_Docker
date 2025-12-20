package com.flightapp.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document(collection = "tickets")
public class Ticket {

	@Id
	private String id;

	@NotBlank
	private String pnr;

	@NotBlank(message = "User email cannot be blank")
	private String userEmail;

	@NotBlank(message = "Departure flight ID cannot be blank")
	private String departureFlightId;

	private String returnFlightId;

	@NotNull(message = "Trip type must be provided")
	private FLIGHTTYPE tripType;

	@NotNull(message = "Booking time cannot be null")
	private LocalDateTime bookingTime;

	private String seatsBooked;

	private String mealType;

	@Min(0)
	private Double totalPrice;
	private boolean canceled;

	@Transient
	private List<Passenger> passengers;
}
