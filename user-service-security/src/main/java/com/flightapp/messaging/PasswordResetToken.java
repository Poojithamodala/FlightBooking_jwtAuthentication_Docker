package com.flightapp.messaging;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "password_reset_tokens") 
public class PasswordResetToken {

    @Id
    private String id;

    private String token;
    private String email;
    private LocalDateTime expiryTime;
}
