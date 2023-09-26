package com.behl.cerberus.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@JacksonStdImpl
public class ExceptionResponseDto {

	private final String status;
	private final String message;
	private final String timestamp;

	public ExceptionResponseDto(@NonNull final String message) {
		this.status = "Failure";
		this.message = message;
		this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toString();
	}

}