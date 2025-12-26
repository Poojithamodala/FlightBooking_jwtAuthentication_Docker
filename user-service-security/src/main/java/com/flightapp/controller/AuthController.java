package com.flightapp.controller;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.messaging.PasswordResetEvent;
import com.flightapp.messaging.PasswordResetToken;
import com.flightapp.model.BlacklistedToken;
import com.flightapp.model.ChangePassword;
import com.flightapp.model.User;
import com.flightapp.repository.PasswordResetTokenRepository;
import com.flightapp.repository.TokenBlacklistRepository;
import com.flightapp.repository.UserRepository;
import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final TokenBlacklistRepository tokenBlacklistRepository;
	private final PasswordResetTokenRepository tokenRepository;
	private final KafkaTemplate<String, PasswordResetEvent> kafkaTemplate;
	private final PasswordEncoder passwordEncoder;

	public AuthController(AuthService authService, UserRepository userRepository,
			TokenBlacklistRepository tokenBlacklistRepository, PasswordResetTokenRepository tokenRepository,
			PasswordEncoder passwordEncoder, KafkaTemplate<String, PasswordResetEvent> kafkaTemplate) {
		this.authService = authService;
		this.userRepository = userRepository;
		this.tokenBlacklistRepository = tokenBlacklistRepository;
		this.tokenRepository = tokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.kafkaTemplate = kafkaTemplate;
	}

	@PostMapping("/register")
	public Mono<ResponseEntity<String>> register(@RequestBody SignUpRequest request) {
		if (request.getEmail() == null || request.getPassword() == null) {
			return Mono.just(ResponseEntity.badRequest().body("Email and password are required"));
		}
		return authService.register(request).map(msg -> ResponseEntity.status(HttpStatus.CREATED).body(msg))
				.onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ex.getMessage())));
	}

	@PostMapping("/login")
	public Mono<ResponseEntity<JwtResponse>> login(@RequestBody LoginRequest request) {
		return authService.login(request).map(ResponseEntity::ok);
	}

	@GetMapping("/profile")
	public Mono<User> getProfile(Authentication authentication) {
		String email = authentication.getName(); // comes from JWT
		return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new RuntimeException("User not found")));
	}

	@PutMapping("/changepassword")
	public Mono<ResponseEntity<Map<String, String>>> changePassword(@AuthenticationPrincipal Jwt jwt,
			@RequestBody ChangePassword request) {

		return authService.changePassword(jwt.getSubject(), request)
				.map(msg -> ResponseEntity.ok(Map.of("message", msg)));
	}

	@PostMapping("/logout")
	public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String token) {
		if (token == null || !token.startsWith("Bearer ")) {
			return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
		}

		String jwt = token.substring(7);

		return tokenBlacklistRepository.save(new BlacklistedToken(jwt, new Date(System.currentTimeMillis() + 3600_000)))
				.map(saved -> ResponseEntity.ok("Logged out successfully")).onErrorResume(ex -> {
					ex.printStackTrace();
					return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
							.body("Error logging out: " + ex.getMessage()));
				});
	}

//	@PostMapping("/forgot-password")
//	public Mono<ResponseEntity<String>> forgotPassword(@AuthenticationPrincipal Jwt jwt) {
//		String email = jwt.getClaimAsString("sub");
//
//		return userRepository.findByEmail(email)
//				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
//				.flatMap(user -> {
//
//					PasswordResetToken token = new PasswordResetToken(null, UUID.randomUUID().toString(),
//							user.getEmail(), LocalDateTime.now().plusMinutes(15));
//
//					return tokenRepository.save(token)
//							.doOnSuccess(saved -> kafkaTemplate.send("password-reset-events",
//									new PasswordResetEvent(user.getEmail(), saved.getToken())))
//							.thenReturn(ResponseEntity.ok("Password reset email sent"));
//				});
//	}
	@PostMapping("/forgot-password")
	public Mono<ResponseEntity<String>> forgotPassword(@RequestParam String email) {
		return userRepository.findByEmail(email)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
				.flatMap(user -> {
					PasswordResetToken token = new PasswordResetToken(null, UUID.randomUUID().toString(),
							user.getEmail(), LocalDateTime.now().plusMinutes(15));
					return tokenRepository.save(token)
							.doOnSuccess(saved -> kafkaTemplate.send("password-reset-events",
									new PasswordResetEvent(user.getEmail(), saved.getToken())))
							.thenReturn(ResponseEntity.ok("Password reset email sent"));
				});
	}

	@PostMapping("/reset-password")
	public Mono<ResponseEntity<String>> resetPassword(@RequestParam String token, @RequestParam String newPassword) {

		return tokenRepository.findByToken(token)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token")))
				.flatMap(resetToken -> {

					if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired"));
					}

					return userRepository.findByEmail(resetToken.getEmail()).flatMap(user -> {
						user.setPassword(passwordEncoder.encode(newPassword));
						return userRepository.save(user);
					}).then(tokenRepository.delete(resetToken))
							.thenReturn(ResponseEntity.ok("Password reset successful"));
				});
	}
}