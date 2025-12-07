package com.flightapp.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingEvent {
	private String eventType;
	private String pnr;
	private String userEmail;
	private Double totalPrice;
}
