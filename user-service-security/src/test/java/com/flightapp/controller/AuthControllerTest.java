package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.flightapp.repository.TokenBlacklistRepository;
import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;
import com.flightapp.service.AuthService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthControllerTest {

    private AuthService authService;
    private TokenBlacklistRepository tokenBlacklistRepository;
    private AuthController authController;

//    @BeforeEach
//    void setUp() {
//        authService = mock(AuthService.class);
//        tokenBlacklistRepository = mock(TokenBlacklistRepository.class);
//        authController = new AuthController(authService, null, tokenBlacklistRepository);
//    }
//
//    @Test
//    void testRegisterSuccess() {
//
//        SignUpRequest request = new SignUpRequest();
//        request.setUsername("pooji");
//        request.setEmail("pooji@gmail.com");
//        request.setPassword("pass");
//        request.setRole("USER");
//
//        when(authService.register(any(SignUpRequest.class)))
//                .thenReturn(Mono.just("User registered successfully with id: 123"));
//
//        Mono<ResponseEntity<String>> result = authController.register(request);
//
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assert response.getStatusCode() == HttpStatus.CREATED;
//                    assert response.getBody().equals("User registered successfully with id: 123");
//                })
//                .verifyComplete();
//
//        verify(authService, times(1)).register(request);
//    }
//
//    @Test
//    void testLoginSuccess() {
//
//        LoginRequest request = new LoginRequest();
//        request.setEmail("pooji@gmail.com");
//        request.setPassword("pass");
//
//        JwtResponse jwtResponse = new JwtResponse("jwt-token-123");
//
//        when(authService.login(any(LoginRequest.class)))
//                .thenReturn(Mono.just(jwtResponse));
//
//        Mono<ResponseEntity<JwtResponse>> result = authController.login(request);
//
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assert response.getStatusCode() == HttpStatus.OK;
//                    assert response.getBody() != null;
//                    assert response.getBody().getToken().equals("jwt-token-123");
//                })
//                .verifyComplete();
//
//        verify(authService, times(1)).login(request);
//    }

//    @Test
//    void testLoginFailure() {
//
//        LoginRequest request = new LoginRequest();
//        request.setEmail("pooji@gmail.com");
//        request.setPassword("wrongpass");
//
//        when(authService.login(any(LoginRequest.class)))
//                .thenReturn(Mono.error(new RuntimeException("Invalid password")));
//
//        Mono<ResponseEntity<JwtResponse>> result = authController.login(request);
//
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assert response.getStatusCode() == HttpStatus.UNAUTHORIZED;
//                    assert response.getBody() == null;
//                })
//                .verifyComplete();
//
//        verify(authService, times(1)).login(request);
//    }
    
//    @Test
//    void testLogoutSuccess() {
//
//        String token = "jwt-token-123";
//
//        when(authService.logout(token))
//                .thenReturn(Mono.just("Logged out successfully"));
//
//        Mono<ResponseEntity<String>> result =
//                authController.logout("Bearer " + token);
//
//        StepVerifier.create(result)
//                .assertNext(response -> {
//                    assert response.getStatusCode() == HttpStatus.OK;
//                    assert response.getBody().equals("Logged out successfully");
//                })
//                .verifyComplete();
//
//        verify(authService, times(1)).logout(token);
//    }
}
