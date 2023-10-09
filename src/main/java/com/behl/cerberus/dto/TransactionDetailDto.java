package com.behl.cerberus.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.behl.cerberus.entity.Currency;
import com.behl.cerberus.entity.TransactionType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class TransactionDetailDto {

	private BigDecimal amount;
	private Currency currency;
	private TransactionType type;
	private LocalDateTime timestamp;

}