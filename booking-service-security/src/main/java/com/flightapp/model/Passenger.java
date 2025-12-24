package com.flightapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document
@CompoundIndex(
		  name = "unique_seat_per_flight",
		  def = "{'flightId': 1, 'seatNumber': 1}",
		  unique = true
		)
public class Passenger {

	@Id
	private String id;
	
	private String flightId;

	@NotBlank(message = "Passenger name cannot be blank")
	private String name;

	@NotBlank(message = "Passenger gender cannot be blank")
	private String gender;

	@NotNull(message = "Passenger age cannot be null")
	@Min(value = 1, message = "Passenger age must be at least 1")
	private Integer age;

	@NotBlank(message = "Seat number cannot be blank")
	private String seatNumber;

	private String mealPreference;

	private String ticketId;
}
