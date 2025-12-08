package com.flightapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public Mono<ResponseEntity<String>> register(@RequestBody SignUpRequest request) {
		return authService.register(request).map(msg -> ResponseEntity.status(HttpStatus.CREATED).body(msg))
				.onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ex.getMessage())));
	}

	@PostMapping("/login")
	public Mono<ResponseEntity<JwtResponse>> login(@RequestBody LoginRequest request) {
		return authService.login(request).map(ResponseEntity::ok).onErrorResume(
				ex -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse(null))));
	}

}