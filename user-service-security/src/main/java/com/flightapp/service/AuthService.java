package com.flightapp.service;

import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;

import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<String> register(SignUpRequest request);
    Mono<JwtResponse> login(LoginRequest request);
    Mono<String> logout(String token);
}
