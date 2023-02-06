package com.behl.cerberus.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserCreationRequestDto implements Serializable {

	private static final long serialVersionUID = -6117448062182178039L;

	@NotBlank(message = "first-name must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "first-name of user", example = "Hardik", maxLength = 15, minLength = 3)
	private final String firstName;

	@Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "last-name of user", example = "Behl", maxLength = 15, minLength = 3)
	private final String lastName;

	@NotBlank(message = "email-id must not be empty")
	@Email(message = "email-id must be of valid format")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "email-id of user", example = "hardik.behl7444@gmail.com")
	private final String emailId;

	@NotBlank(message = "password must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "secure password to enable user login", example = "somethingSecure")
	private final String password;

}
