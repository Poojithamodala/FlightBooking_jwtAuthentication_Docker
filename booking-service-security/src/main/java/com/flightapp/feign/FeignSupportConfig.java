package com.flightapp.feign;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignSupportConfig {

	@Bean
	public HttpMessageConverters httpMessageConverters() {
		return new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
	}
}