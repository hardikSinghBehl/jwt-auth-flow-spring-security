package com.behl.cerberus.dto;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserCreationRequestDto implements Serializable {

	private static final long serialVersionUID = -6117448062182178039L;

	@NotBlank
	@Schema(required = true, description = "first-name of user", example = "Hardik", maxLength = 15, minLength = 3)
	private final String firstName;

	@NotBlank
	@Schema(required = false, description = "last-name of user", example = "Behl", maxLength = 15, minLength = 3)
	private final String lastName;

	@NotBlank
	@Email
	@Schema(required = true, description = "email-id of user", example = "hardik.behl7444@gmail.com")
	private final String emailId;

	@NotBlank
	@Schema(required = true, description = "secure password to enable user login", example = "somethingSecure")
	private final String password;

}
