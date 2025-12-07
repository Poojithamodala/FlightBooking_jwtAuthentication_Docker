package com.flightapp.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.flightapp.messaging.BookingEvent;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

	@Mock
	private JavaMailSender mailSender;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Test
	void handleBookingEvent_bookingConfirmed_sendsExpectedEmail() {
		BookingEvent event = new BookingEvent();
		event.setEventType("BOOKING_CONFIRMED");
		event.setPnr("PNR123");
		event.setUserEmail("pooja@gmail.com");
		event.setTotalPrice(1500.0);

		notificationService.handleBookingEvent(event);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender, times(1)).send(captor.capture());

		SimpleMailMessage message = captor.getValue();
		assertThat(message.getTo()).containsExactly("pooja@gmail.com");
		assertThat(message.getSubject()).isEqualTo("Your flight booking is confirmed - PNR PNR123");
		assertThat(message.getText()).contains("Your PNR is PNR123").contains("total price is 1500.0");
	}

	@Test
	void handleBookingEvent_bookingCancelled_sendsExpectedEmail() {
		BookingEvent event = new BookingEvent();
		event.setEventType("BOOKING_CANCELLED");
		event.setPnr("PNR456");
		event.setUserEmail("pooja2@gmail.com");
		event.setTotalPrice(0.0);

		notificationService.handleBookingEvent(event);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender, times(1)).send(captor.capture());

		SimpleMailMessage message = captor.getValue();
		assertThat(message.getTo()).containsExactly("pooja2@gmail.com");
		assertThat(message.getSubject()).isEqualTo("Your flight booking is cancelled - PNR PNR456");
		assertThat(message.getText()).isEqualTo("Your booking with PNR PNR456 has been cancelled.");
	}

	@Test
	void handleBookingEvent_unknownType_usesDefaultSubjectAndBody() {
		BookingEvent event = new BookingEvent();
		event.setEventType("SOME_OTHER_EVENT");
		event.setPnr("PNR789");
		event.setUserEmail("pooja@gmail.com");
		event.setTotalPrice(999.0);

		notificationService.handleBookingEvent(event);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender, times(1)).send(captor.capture());

		SimpleMailMessage message = captor.getValue();
		assertThat(message.getTo()).containsExactly("pooja@gmail.com");
		assertThat(message.getSubject()).isEqualTo("Flight booking update");
		assertThat(message.getText()).isEqualTo("Update for booking PNR: PNR789");
	}

	@Test
	void handleBookingEvent_mailSenderThrows_exceptionIsCaught() {
		BookingEvent event = new BookingEvent();
		event.setEventType("BOOKING_CONFIRMED");
		event.setPnr("PNR999");
		event.setUserEmail("pooja@gmail.com");
		event.setTotalPrice(123.0);

		doThrow(new MailSendException("fail")).when(mailSender).send(any(SimpleMailMessage.class));

		notificationService.handleBookingEvent(event);

		verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
	}
}