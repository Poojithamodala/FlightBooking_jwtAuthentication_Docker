package com.flightapp.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flightapp.model.BlacklistedToken;
import com.flightapp.model.ROLE;
import com.flightapp.model.User;
import com.flightapp.repository.TokenBlacklistRepository;
import com.flightapp.repository.UserRepository;
import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;
import com.flightapp.service.AuthService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepository;
	private final TokenBlacklistRepository tokenBlacklistRepository;
	private final PasswordEncoder passwordEncoder;

	public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenBlacklistRepository tokenBlacklistRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenBlacklistRepository=tokenBlacklistRepository;
	}

	@Value("${spring.security.oauth2.resourceserver.jwt.secret}")
	private String secret;

	@Override
	public Mono<String> register(SignUpRequest request) {
		return userRepository.findByEmail(request.getEmail())
				.flatMap(existing -> Mono.<String>error(new RuntimeException("User already exists")))
				.switchIfEmpty(Mono.defer(() -> {
					User user = new User();
					user.setUsername(request.getUsername());
					user.setEmail(request.getEmail());
					user.setPassword(passwordEncoder.encode(request.getPassword()));
					user.setRole("ADMIN".equalsIgnoreCase(request.getRole()) ? ROLE.ADMIN : ROLE.USER);

					return userRepository.save(user)
							.map(saved -> "User registered successfully. ");
				}));
	}
	

	@Override
	public Mono<JwtResponse> login(LoginRequest request) {

	    if (request.getEmail() == null || request.getPassword() == null) {
	        return Mono.error(new RuntimeException("Email and password must not be null"));
	    }

	    return userRepository.findByEmail(request.getEmail())
	        .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
	        .flatMap(user -> {

	            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	                return Mono.error(new RuntimeException("Invalid password"));
	            }

	            if (secret == null || secret.isEmpty()) {
	                return Mono.error(new RuntimeException("JWT secret is not configured"));
	            }

	            try {
	                String token = Jwts.builder()
	                        .setSubject(user.getEmail())               // ✅ email as subject
	                        .claim("role", user.getRole().name())
	                        .setIssuedAt(new Date())
	                        .setExpiration(
	                                new Date(System.currentTimeMillis() + 3600_000) // 1 hour
	                        )
	                        .signWith(
	                                SignatureAlgorithm.HS256,
	                                secret.getBytes(StandardCharsets.UTF_8)
	                        )
	                        .compact();

	                return Mono.just(new JwtResponse(token));

	            } catch (Exception e) {
	                return Mono.error(
	                        new RuntimeException("Failed to generate JWT: " + e.getMessage(), e)
	                );
	            }
	        });
	}
	
	@Override
	public Mono<String> logout(String token) {
	    return tokenBlacklistRepository
	        .save(new BlacklistedToken(token,
	                new Date(System.currentTimeMillis() + 3600_000)))
	        .map(saved -> "Logged out successfully")
	        .onErrorResume(DuplicateKeyException.class,
	            ex -> Mono.just("Already logged out"));
	}

}