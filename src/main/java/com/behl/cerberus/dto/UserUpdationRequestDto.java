package com.behl.cerberus.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;

@Getter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class UserUpdationRequestDto {

	@JsonAlias("firstName")
	@Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "first-name of user", example = "Hardik")
	private String firstName;

	@JsonAlias("lastName")
	@Schema(requiredMode = RequiredMode.NOT_REQUIRED, description = "last-name of user", example = "Singh")
	private String lastName;

}