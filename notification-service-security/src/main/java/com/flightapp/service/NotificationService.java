package com.flightapp.service;

import com.flightapp.messaging.BookingEvent;

public interface NotificationService {
	void handleBookingEvent(BookingEvent event);
}
