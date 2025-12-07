package com.flightapp.messaging;

import lombok.Data;

@Data
public class BookingEvent {
	private String eventType;
	private String pnr;
	private String userEmail;
	private Double totalPrice;
}
