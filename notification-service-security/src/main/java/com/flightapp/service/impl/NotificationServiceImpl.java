package com.flightapp.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.flightapp.messaging.BookingEvent;
import com.flightapp.messaging.PasswordResetEvent;
import com.flightapp.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

	private final JavaMailSender mailSender;

	@Override
	@KafkaListener(topics = "booking-events")
	public void handleBookingEvent(BookingEvent event) {
		log.info("Received booking event: {}", event);

		String subject;
		String body;

		switch (event.getEventType()) {
		case "BOOKING_CONFIRMED":
			subject = "Your flight booking is confirmed - PNR " + event.getPnr();
			body = "Thank you for booking. Your PNR is " + event.getPnr() + " and total price is "
					+ event.getTotalPrice();
			break;

		case "BOOKING_CANCELLED":
			subject = "Your flight booking is cancelled - PNR " + event.getPnr();
			body = "Your booking with PNR " + event.getPnr() + " has been cancelled.";
			break;

		default:
			subject = "Flight booking update";
			body = "Update for booking PNR: " + event.getPnr();
			break;
		}
		log.info("Sending email to {}", event.getUserEmail());

		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("poojithamodala26@gmail.com");
			message.setTo(event.getUserEmail());
			message.setSubject(subject);
			message.setText(body);
			mailSender.send(message);
		} catch (Exception e) {
			log.error("Failed to send email", e);
		}
	}
	
	@Override
    @KafkaListener(topics = "password-reset-events")
    public void handlePasswordReset(PasswordResetEvent event) {

        String resetLink = "http://localhost:4200/reset-password?token=" + event.getToken();
        String subject = "Reset your password";
        String body = "Click the link below to reset your password:\n\n"
                    + resetLink + "\n\nThis link is valid for 15 minutes.";

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("poojithamodala26@gmail.com");
            message.setTo(event.getEmail());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", event.getEmail(), e);
        }
    }
}
