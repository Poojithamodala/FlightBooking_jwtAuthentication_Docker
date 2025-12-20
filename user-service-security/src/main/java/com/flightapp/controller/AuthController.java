package com.flightapp.controller;

import java.util.Date;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.model.BlacklistedToken;
import com.flightapp.model.User;
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

	public AuthController(AuthService authService, UserRepository userRepository,
			TokenBlacklistRepository tokenBlacklistRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
		this.tokenBlacklistRepository = tokenBlacklistRepository;
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
		if (request.getEmail() == null || request.getPassword() == null) {
			return Mono.just(ResponseEntity.badRequest().build());
		}
		return authService.login(request).map(ResponseEntity::ok).onErrorResume(
				ex -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse(null))));
	}

	@GetMapping("/profile")
	public Mono<User> getProfile(Authentication authentication) {
		String email = authentication.getName(); // comes from JWT
		return userRepository.findByEmail(email).switchIfEmpty(Mono.error(new RuntimeException("User not found")));
	}

	@PostMapping("/logout")
	public Mono<ResponseEntity<String>> logout(@RequestHeader("Authorization") String token) {
		if (token == null || !token.startsWith("Bearer ")) {
			return Mono.just(ResponseEntity.badRequest().body("Invalid token"));
		}

		String jwt = token.substring(7); 

		return tokenBlacklistRepository.save(new BlacklistedToken(jwt, new Date(System.currentTimeMillis() + 3600_000)))
				.map(saved -> ResponseEntity.ok("Logged out successfully"))
				.onErrorResume(ex -> {
				    ex.printStackTrace();
				    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				                                   .body("Error logging out: " + ex.getMessage()));
				});
	}
}