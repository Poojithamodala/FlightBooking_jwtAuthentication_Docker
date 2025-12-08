package com.flightapp.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import feign.RequestInterceptor;
import reactor.core.publisher.Mono;

@Configuration
public class FeignClientConfig {

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			Mono<Object> authHeaderMono = Mono.deferContextual(ctx -> {
				if (ctx.hasKey(HttpHeaders.AUTHORIZATION)) {
					return Mono.just(ctx.get(HttpHeaders.AUTHORIZATION));
				}
				return Mono.empty();
			});

			authHeaderMono.subscribe(auth -> {
				if (auth != null) {
					requestTemplate.header(HttpHeaders.AUTHORIZATION, auth.toString());
				}
			});
		};
	}

	@Bean
	public RequestInterceptor authInterceptor() {
		return requestTemplate -> {
			requestTemplate.header("Authorization", "Bearer <your-token-here>");
		};
	}
}