package com.flightapp.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignClientInterceptor implements RequestInterceptor {

	private final String token;

	public FeignClientInterceptor(String token) {
		this.token = token;
	}

	@Override
	public void apply(RequestTemplate template) {
		template.header("Authorization", token);
	}
}
