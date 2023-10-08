package com.behl.cerberus.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class IdentityVerificationRequestDto {

	@Past
	@NotNull(message = "Date of birth must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "Date of birth in the format YYYY-MM-DD", example = "1970-01-15")
	private LocalDate dateOfBirth;
	
	@NotBlank(message = "Street address must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "Current residential street address", example = "12/3A Main Street")
	private String streetAddress;
	
	@NotBlank(message = "City must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "Current residential city", example = "New Delhi")
	private String city;
	
	@NotBlank(message = "State must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "Current residential state", example = "Delhi")
	private String state;
	
	@NotBlank(message = "Postal code must not be empty")
	@Pattern(regexp = "^\\d{6}$", message = "Postal code must be a 6-digit number")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "Current residential postal code", example = "001002")
	private String postalCode;

}

