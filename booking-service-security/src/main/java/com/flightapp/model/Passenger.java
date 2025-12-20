package com.flightapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document
public class Passenger {

	@Id
	private String id;

	@NotBlank(message = "Passenger name cannot be blank")
	private String name;

	@NotBlank(message = "Passenger gender cannot be blank")
	private String gender;

	@NotNull(message = "Passenger age cannot be null")
	@Min(value = 1, message = "Passenger age must be at least 1")
	private Integer age;

	@NotBlank(message = "Seat number cannot be blank")
	private String seatNumber;
	
	private String flightId;

	private String mealPreference;

	private String ticketId;
}
