package com.flightapp.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.flightapp.model.BlacklistedToken;
import com.flightapp.model.ROLE;
import com.flightapp.model.User;
import com.flightapp.repository.TokenBlacklistRepository;
import com.flightapp.repository.UserRepository;
import com.flightapp.request.LoginRequest;
import com.flightapp.request.SignUpRequest;
import com.flightapp.response.JwtResponse;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private TokenBlacklistRepository tokenBlacklistRepository;
    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenBlacklistRepository = mock(TokenBlacklistRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                tokenBlacklistRepository
        );

        // Set JWT secret for login tests
        ReflectionTestUtils.setField(authService, "secret", "test-secret-key");
    }

    // ---------- REGISTER TESTS ----------

//    @Test
//    void testRegisterSuccess() {
//
//        SignUpRequest request = new SignUpRequest();
//        request.setEmail("pooja@gmail.com");
//        request.setUsername("Poojitha");
//        request.setPassword("password");
//        request.setRole("USER");
//
//        when(userRepository.findByEmail(request.getEmail()))
//                .thenReturn(Mono.empty());
//
//        when(passwordEncoder.encode("password"))
//                .thenReturn("encoded-pass");
//
//        User savedUser = new User();
//        savedUser.setId("123");
//
//        when(userRepository.save(any(User.class)))
//                .thenReturn(Mono.just(savedUser));
//
//        StepVerifier.create(authService.register(request))
//                .expectNext("User registered successfully with id: 123")
//                .verifyComplete();
//
//        verify(userRepository).findByEmail(request.getEmail());
//        verify(userRepository).save(any(User.class));
//    }

    @Test
    void testRegisterUserAlreadyExists() {

        SignUpRequest request = new SignUpRequest();
        request.setEmail("pooja@gmail.com");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Mono.just(new User()));

        StepVerifier.create(authService.register(request))
                .expectErrorMessage("User already exists")
                .verify();
    }

    // ---------- LOGIN TESTS ----------

    @Test
    void testLoginSuccess() {

        LoginRequest request = new LoginRequest();
        request.setEmail("poojitha@gmail.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail("poojitha@gmail.com");
        user.setPassword("encoded-pass");
        user.setRole(ROLE.USER);

        when(userRepository.findByEmail("poojitha@gmail.com"))
                .thenReturn(Mono.just(user));

        when(passwordEncoder.matches("password", "encoded-pass"))
                .thenReturn(true);

        StepVerifier.create(authService.login(request))
                .expectNextMatches(jwt ->
                        jwt instanceof JwtResponse && jwt.getToken() != null
                )
                .verifyComplete();

        verify(userRepository).findByEmail("poojitha@gmail.com");
    }

    @Test
    void testLoginUserNotFound() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("dummy");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Mono.empty());

        StepVerifier.create(authService.login(request))
                .expectErrorMessage("User not found")
                .verify();
    }

    @Test
    void testLoginInvalidPassword() {

        LoginRequest request = new LoginRequest();
        request.setEmail("poojitha@gmail.com");
        request.setPassword("wrongPass");

        User user = new User();
        user.setEmail("poojitha@gmail.com");
        user.setPassword("encoded-pass");

        when(userRepository.findByEmail("poojitha@gmail.com"))
                .thenReturn(Mono.just(user));

        when(passwordEncoder.matches("wrongPass", "encoded-pass"))
                .thenReturn(false);

        StepVerifier.create(authService.login(request))
                .expectErrorMessage("Invalid password")
                .verify();
    }

    // ---------- LOGOUT TEST ----------

//    @Test
//    void testLogoutSuccess() {
//
//        String token = "dummy-jwt-token";
//
//        when(tokenBlacklistRepository.save(any(BlacklistedToken.class)))
//                .thenAnswer(invocation -> {
//                    BlacklistedToken tokenEntity = invocation.getArgument(0);
//                    tokenEntity.setExpiryDate(new Date());
//                    return Mono.just(tokenEntity);
//                });
//
//        StepVerifier.create(authService.logout(token))
//                .expectNext("Logged out successfully")
//                .verifyComplete();
//
//        verify(tokenBlacklistRepository).save(any(BlacklistedToken.class));
//    }
}
