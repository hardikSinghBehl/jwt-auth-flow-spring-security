package com.behl.cerberus.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Schema(title = "UserUpdationRequest", accessMode = Schema.AccessMode.WRITE_ONLY)
public class UserUpdationRequestDto {

	@NotBlank(message = "first-name must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "first-name of user", example = "Hardik")
	private String firstName;

	@Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "last-name of user", example = "Behl")
	private String lastName;

}