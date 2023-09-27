package com.behl.cerberus.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class UserCreationRequestDto {

	@NotBlank(message = "first-name must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "first-name of user", example = "Hardik")
	private String firstName;

	@Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "last-name of user", example = "Behl")
	private String lastName;

	@NotBlank(message = "email-id must not be empty")
	@Email(message = "email-id must be of valid format")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "email-id of user", example = "hardik.behl7444@gmail.com")
	private String emailId;

	@NotBlank(message = "password must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "secure password to enable user login", example = "somethingSecure")
	private String password;

}
