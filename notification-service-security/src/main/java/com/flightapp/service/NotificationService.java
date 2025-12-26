package com.flightapp.service;

import com.flightapp.messaging.BookingEvent;
import com.flightapp.messaging.PasswordResetEvent;

public interface NotificationService {
	void handleBookingEvent(BookingEvent event);
	void handlePasswordReset(PasswordResetEvent event);
}
