package com.flightapp.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flightapp.model.User;
import com.flightapp.repository.UserRepository;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthServiceImplTest {

	private UserRepository userRepository;
	private AuthServiceImpl authService;

//	@BeforeEach
//	void setUp() {
//		userRepository = mock(UserRepository.class);
//		authService = new AuthServiceImpl(userRepository);
//	}

//	@Test
//	void testRegisterSuccess() {
//		User user = new User();
//		user.setEmail("pooja@gmail.com");
//
//		when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.empty());
//		when(userRepository.save(user)).thenReturn(Mono.just(user));
//
//		StepVerifier.create(authService.register(user)).expectNext(user).verifyComplete();
//
//		verify(userRepository).findByEmail(user.getEmail());
//		verify(userRepository).save(user);
//	}
//
//	@Test
//	void testLoginSuccess() {
//		User user = new User();
//		user.setEmail("pooja@gmail.com");
//		user.setPassword("password");
//
//		when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
//
//		StepVerifier.create(authService.login(user.getEmail(), "password"))
//				.expectNextMatches(sessionId -> sessionId != null && !sessionId.isEmpty()).verifyComplete();
//
//		verify(userRepository).findByEmail(user.getEmail());
//	}
//
//	@Test
//	void testLoginUserNotFound() {
//		when(userRepository.findByEmail("hehehe@gmail.com")).thenReturn(Mono.empty());
//
//		StepVerifier.create(authService.login("hehehe@gmail.com", "password"))
//				.expectErrorMessage("User not found").verify();
//
//		verify(userRepository).findByEmail("hehehe@gmail.com");
//	}
//
//	@Test
//	void testLoginInvalidPassword() {
//		User user = new User();
//		user.setEmail("pooja@gmail.com");
//		user.setPassword("Password");
//
//		when(userRepository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
//
//		StepVerifier.create(authService.login(user.getEmail(), "wrongPass")).expectErrorMessage("Invalid password")
//				.verify();
//
//		verify(userRepository).findByEmail(user.getEmail());
//	}
}
