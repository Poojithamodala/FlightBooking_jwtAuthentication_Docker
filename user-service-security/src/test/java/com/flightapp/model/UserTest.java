package com.flightapp.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class UserTest {

	private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
	private final Validator validator = factory.getValidator();

	@Test
	void testUserCreationAndGettersSetters() {

		User user = new User();
		user.setId("1");
		user.setUsername("Poojitha");
		user.setEmail("poojitha@gmail.com");
		user.setPassword("securePassword");
		user.setRole(ROLE.USER);

		assertEquals("1", user.getId());
		assertEquals("Poojitha", user.getUsername());
		assertEquals("poojitha@gmail.com", user.getEmail());
		assertEquals("securePassword", user.getPassword());
		assertEquals(ROLE.USER, user.getRole());
	}

	@Test
	void testUserValidationConstraints() {

		User user = new User();

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertFalse(violations.isEmpty());

		assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username cannot be null")));
		assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Email cannot be null")));
		assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Age cannot be null")));
		assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Gender cannot be null")));
	}
}
