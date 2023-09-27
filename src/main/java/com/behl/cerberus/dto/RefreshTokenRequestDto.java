package com.behl.cerberus.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class RefreshTokenRequestDto {

	@NotBlank(message = "Refresh token must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, description = "refresh-token received during successfull login")
	private String refreshToken;

}
