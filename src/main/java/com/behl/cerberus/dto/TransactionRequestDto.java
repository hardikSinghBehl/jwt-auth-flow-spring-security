package com.behl.cerberus.dto;

import java.math.BigDecimal;

import com.behl.cerberus.entity.Currency;
import com.behl.cerberus.entity.TransactionType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class TransactionRequestDto {

	@NotNull(message = "Amount must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "100.00")
	@DecimalMin(value = "0.01", message = "Amount must be greater than or equal to 0.01")
	private BigDecimal amount;

	@NotNull(message = "Currency must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "USD")
	private Currency currency;

	@NotNull(message = "Transaction type must not be empty")
	@Schema(requiredMode = RequiredMode.REQUIRED, example = "DEPOSIT")
	private TransactionType type;

}