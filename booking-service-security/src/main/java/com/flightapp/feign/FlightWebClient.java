package com.flightapp.feign;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.flightapp.dto.FlightDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FlightWebClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<FlightDto> getFlight(String flightId, String auth) {
        return webClientBuilder.build()
                .get()
                .uri("http://FLIGHT-SERVICE/api/flight/{id}", flightId)
                .header("Authorization", auth)
                .retrieve()
                .bodyToMono(FlightDto.class);
    }
}
