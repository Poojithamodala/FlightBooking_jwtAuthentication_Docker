package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.flightapp.model.User;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthControllerTest {

	private AuthService authService;
	private AuthController authController;

	@BeforeEach
	void setUp() {
		authService = mock(AuthService.class);
		authController = new AuthController(authService);
	}

//	@Test
//	void testRegisterSuccess() {
//		User user = new User();
//		user.setId("123");
//		when(authService.register(any(User.class))).thenReturn(Mono.just(user));
//
//		Mono<String> result = authController.register(user);
//
//		StepVerifier.create(result).expectNext("user created with id: 123").verifyComplete();
//
//		verify(authService, times(1)).register(user);
//	}
//
//	@Test
//	void testLoginSuccess() {
//		User user = new User();
//		user.setEmail("pooja@gmail.com");
//		user.setPassword("password");
//
//		when(authService.login(user.getEmail(), user.getPassword())).thenReturn(Mono.just("session-123"));
//
//		Mono<ResponseEntity<Map<String, String>>> result = authController.login(user);
//
//		StepVerifier.create(result)
//				.expectNextMatches(response -> response.getStatusCode() == HttpStatus.OK
//						&& response.getBody().get("sessionId").equals("session-123")
//						&& response.getBody().get("message").equals("Login successful"))
//				.verifyComplete();
//
//		verify(authService, times(1)).login(user.getEmail(), user.getPassword());
//	}
//
//	@Test
//	void testLoginFailure() {
//		User user = new User();
//		user.setEmail("pooja@gmail.com");
//		user.setPassword("wrongpass");
//
//		when(authService.login(user.getEmail(), user.getPassword()))
//				.thenReturn(Mono.error(new RuntimeException("Invalid credentials")));
//
//		Mono<ResponseEntity<Map<String, String>>> result = authController.login(user);
//
//		StepVerifier.create(result).expectNextMatches(response -> response.getStatusCode() == HttpStatus.UNAUTHORIZED
//				&& response.getBody().get("error").equals("Invalid credentials")).verifyComplete();
//
//		verify(authService, times(1)).login(user.getEmail(), user.getPassword());
//	}
}
