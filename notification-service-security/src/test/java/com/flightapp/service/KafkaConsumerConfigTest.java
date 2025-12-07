package com.flightapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.flightapp.messaging.BookingEvent;

class KafkaConsumerConfigTest {

	private final KafkaConsumerConfig config = new KafkaConsumerConfig();

	@Test
	void bookingEventConsumerFactory_shouldCreateProperlyConfiguredConsumerFactory() {
		ConsumerFactory<String, BookingEvent> factory = config.bookingEventConsumerFactory();

		assertNotNull(factory, "ConsumerFactory should not be null");
		assertTrue(factory instanceof DefaultKafkaConsumerFactory, "Factory should be a DefaultKafkaConsumerFactory");

		@SuppressWarnings("unchecked")
		DefaultKafkaConsumerFactory<String, BookingEvent> defaultFactory = (DefaultKafkaConsumerFactory<String, BookingEvent>) factory;

		Map<String, Object> configs = defaultFactory.getConfigurationProperties();
		assertEquals("localhost:9092", configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG),
				"Bootstrap servers should be localhost:9092");

		assertEquals("notification-service", configs.get(ConsumerConfig.GROUP_ID_CONFIG),
				"Group id should be notification-service");

		assertEquals(StringDeserializer.class, configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG),
				"Key deserializer should be StringDeserializer");

		assertEquals(org.springframework.kafka.support.serializer.JsonDeserializer.class,
				configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG),
				"Value deserializer should be JsonDeserializer class");
	}

	@Test
	void bookingEventKafkaListenerContainerFactory_shouldUseConsumerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, BookingEvent> factory = config
				.bookingEventKafkaListenerContainerFactory();

		assertNotNull(factory, "KafkaListenerContainerFactory should not be null");
		assertNotNull(factory.getConsumerFactory(), "ConsumerFactory inside factory should not be null");

		assertTrue(factory.getConsumerFactory() instanceof DefaultKafkaConsumerFactory,
				"ConsumerFactory should be DefaultKafkaConsumerFactory");
	}
}