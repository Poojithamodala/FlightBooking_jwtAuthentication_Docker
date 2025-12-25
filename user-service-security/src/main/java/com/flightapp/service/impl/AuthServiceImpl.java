package com.flightapp.service.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.exception.BadRequestException;
import com.flightapp.exception.EmailNotFoundException;
import com.flightapp.exception.InvalidPasswordException;
import com.flightapp.model.BlacklistedToken;
import com.flightapp.model.ChangePassword;
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

	public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
			TokenBlacklistRepository tokenBlacklistRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenBlacklistRepository = tokenBlacklistRepository;
	}

	@Value("${spring.security.oauth2.resourceserver.jwt.secret}")
	private String secret;
	
	private static final long PASSWORD_EXPIRY_DAYS = 90;

	@Override
	public Mono<String> register(SignUpRequest request) {
		return userRepository.findByEmail(request.getEmail())
				.flatMap(existing -> Mono.<String>error(new RuntimeException("User already exists")))
				.switchIfEmpty(Mono.defer(() -> {
					User user = new User();
					user.setUsername(request.getUsername());
					user.setEmail(request.getEmail());
					user.setPassword(passwordEncoder.encode(request.getPassword()));
					user.setPasswordLastChangedAt(LocalDateTime.now());
					user.setForcePasswordChange(false);
					user.setRole("ADMIN".equalsIgnoreCase(request.getRole()) ? ROLE.ADMIN : ROLE.USER);

					user.setAge(request.getAge());
					user.setGender(request.getGender());

					return userRepository.save(user).map(saved -> "User registered successfully. ");
				}));
	}

	@Override
	public Mono<JwtResponse> login(LoginRequest request) {

		if (request.getEmail() == null || request.getPassword() == null) {
			return Mono.error(new RuntimeException("Email and password must not be null"));
		}

		return userRepository.findByEmail(request.getEmail())
				.switchIfEmpty(Mono.error(new EmailNotFoundException("Email not found"))).flatMap(user -> {

					if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
						return Mono.error(new InvalidPasswordException("Wrong password"));
					}

					boolean forceChange = false;

		            if (user.getPasswordLastChangedAt() == null) {
		                forceChange = true;
		            } else {
		                long days = ChronoUnit.DAYS.between(
		                    user.getPasswordLastChangedAt(),
		                    LocalDateTime.now()
		                );
		                if (days >= 90) {
		                    forceChange = true;
		                }
		            }

		            user.setForcePasswordChange(forceChange);

		            return userRepository.save(user)
		                .map(savedUser -> {

		                    String token = Jwts.builder()
		                        .setSubject(savedUser.getEmail())
		                        .claim("roles", List.of(savedUser.getRole().name()))
		                        // ✅ ADD THIS CLAIM
		                        .claim("forcePasswordChange", savedUser.isForcePasswordChange())
		                        .setIssuedAt(new Date())
		                        .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
		                        .signWith(SignatureAlgorithm.HS256, secret.getBytes(StandardCharsets.UTF_8))
		                        .compact();

		                    return new JwtResponse(token);});
				});
	}

	@Override
	public Mono<String> changePassword(String email, ChangePassword request) {

		return userRepository.findByEmail(email)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
				.flatMap(user -> {

					if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
						return Mono.error(new BadRequestException("Old password is incorrect"));
					}

					if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"New password must be different from old password"));
					}

					if (request.getNewPassword().length() < 6) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Password must be at least 6 characters long"));
					}

					String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$";

					if (!request.getNewPassword().matches(passwordRegex)) {
						return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST,
								"Password must contain uppercase, lowercase, number, and special character"));
					}

					user.setPassword(passwordEncoder.encode(request.getNewPassword()));
					user.setPasswordLastChangedAt(LocalDateTime.now());
		            user.setForcePasswordChange(false);
					return userRepository.save(user);
				}).thenReturn("Password changed successfully");
	}

	@Override
	public Mono<String> logout(String token) {
		return tokenBlacklistRepository
				.save(new BlacklistedToken(token, new Date(System.currentTimeMillis() + 3600_000)))
				.map(saved -> "Logged out successfully")
				.onErrorResume(DuplicateKeyException.class, ex -> Mono.just("Already logged out"));
	}

}