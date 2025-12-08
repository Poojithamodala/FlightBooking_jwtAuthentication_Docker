package com.flightapp.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.flightapp.model.ROLE;
import com.flightapp.model.User;
import com.flightapp.repository.UserRepository;
import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthServiceImplTest {

	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private AuthServiceImpl authService;

	@BeforeEach
	void setUp() {
		userRepository = mock(UserRepository.class);
		passwordEncoder = mock(PasswordEncoder.class);

		authService = new AuthServiceImpl(userRepository, passwordEncoder);
	}

	@Test
	void testRegisterSuccess() {

		SignUpRequest request = new SignUpRequest();
		request.setEmail("pooja@gmail.com");
		request.setUsername("Poojitha");
		request.setPassword("password");
		request.setRole("USER");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.empty());

		when(passwordEncoder.encode("password")).thenReturn("encoded-pass");

		User savedUser = new User();
		savedUser.setId("123");

		when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

		StepVerifier.create(authService.register(request)).expectNext("User registered successfully with id: 123")
				.verifyComplete();

		verify(userRepository).findByEmail(request.getEmail());
		verify(userRepository).save(any(User.class));
	}

	@Test
	void testRegisterUserAlreadyExists() {

		SignUpRequest request = new SignUpRequest();
		request.setEmail("pooja@gmail.com");

		when(userRepository.findByEmail(request.getEmail())).thenReturn(Mono.just(new User()));

		StepVerifier.create(authService.register(request)).expectErrorMessage("User already exists").verify();
	}

	@Test
	void testLoginSuccess() {

		ReflectionTestUtils.setField(authService, "secret", "test-secret-key");

		LoginRequest request = new LoginRequest();
		request.setUsername("Poojitha");
		request.setPassword("password");

		User user = new User();
		user.setUsername("Poojitha");
		user.setPassword("encoded-pass");
		user.setRole(ROLE.USER);

		when(userRepository.findByUsername("Poojitha")).thenReturn(Mono.just(user));

		when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

		StepVerifier.create(authService.login(request)).expectNextMatches(response -> response.getToken() != null)
				.verifyComplete();

		verify(userRepository).findByUsername("Poojitha");
	}

	@Test
	void testLoginUserNotFound() {

		LoginRequest request = new LoginRequest();
		request.setUsername("Unknown");
		request.setPassword("dummy");

		when(userRepository.findByUsername("Unknown")).thenReturn(Mono.empty());

		StepVerifier.create(authService.login(request)).expectErrorMessage("User not found").verify();
	}

	@Test
	void testLoginInvalidPassword() {

		LoginRequest request = new LoginRequest();
		request.setUsername("Poojitha");
		request.setPassword("wrongPass");

		User user = new User();
		user.setUsername("Poojitha");
		user.setPassword("encoded-pass");

		when(userRepository.findByUsername("Poojitha")).thenReturn(Mono.just(user));

		when(passwordEncoder.matches("wrongPass", "encoded-pass")).thenReturn(false);

		StepVerifier.create(authService.login(request)).expectErrorMessage("Invalid password").verify();
	}
}
