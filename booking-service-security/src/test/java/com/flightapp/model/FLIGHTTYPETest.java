package com.flightapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FLIGHTTYPETest {

	@Test
	void testEnumValues() {

		assertEquals("ONE_WAY", FLIGHTTYPE.ONE_WAY.name());
		assertEquals("ROUND_TRIP", FLIGHTTYPE.ROUND_TRIP.name());
	}
}
