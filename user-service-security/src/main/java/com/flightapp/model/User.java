package com.flightapp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Document("users")
public class User {

	@Id
	private String id;

	@NotBlank(message = "Username cannot be null")
    private String username;

	@NotBlank(message = "Email cannot be null")
	private String email;

	@NotNull(message = "Age cannot be null")
	private Integer age;

	@NotBlank(message = "Gender cannot be null")
	private String gender;
	
	@NotBlank(message = "Password cannot be null")
	private String password;

	private ROLE role;
}
