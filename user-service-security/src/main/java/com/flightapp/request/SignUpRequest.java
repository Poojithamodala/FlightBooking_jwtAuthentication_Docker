package com.flightapp.request;

import lombok.Data;

@Data
public class SignUpRequest {
    private String username;
    private String email;
    private String password;
    private String role; 
    
    private Integer age;
    private String gender;
}
