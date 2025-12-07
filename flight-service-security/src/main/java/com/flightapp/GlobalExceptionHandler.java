package com.flightapp;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleResponseStatusException(ResponseStatusException ex) {
        return Mono.just(
                ResponseEntity.status(ex.getStatusCode())
                        .body(Map.of("error", ex.getReason()))
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, String>>> handleGenericException(Exception ex) {
        return Mono.just(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Something went wrong"))
        );
    }
    
    @ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleValidationException(
            org.springframework.web.bind.support.WebExchangeBindException ex) {

        Map<String, String> errors = new java.util.HashMap<>();

        ex.getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        return Mono.just(
                ResponseEntity.badRequest().body(errors)
        );
    }

    
}