package com.flightapp.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "token_blacklist")
public class BlacklistedToken {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String token;
    private Date expiryDate;
    
    public BlacklistedToken(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }
    
    public BlacklistedToken() {}
}
