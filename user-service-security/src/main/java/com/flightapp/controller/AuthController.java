package com.flightapp.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.repository.TokenBlacklistRepository;
import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final TokenBlacklistRepository tokenBlacklistRepository;

	public AuthController(AuthService authService, TokenBlacklistRepository tokenBlacklistRepository) {
		this.authService = authService;
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

	@PostMapping("/logout")
	public Mono<ResponseEntity<String>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		String token = authHeader.replace("Bearer ", "");
		return authService.logout(token).map(msg -> ResponseEntity.ok(msg))
				.onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ex.getMessage())));
	}

	@GetMapping("/token/blacklisted")
	public Mono<Boolean> isTokenBlacklisted(@RequestParam String token) {
		return tokenBlacklistRepository.findByToken(token).map(t -> true).defaultIfEmpty(false);
	}

}