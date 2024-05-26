package com.behl.cerberus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.NonNull;

public class InvalidCredentialsException extends ResponseStatusException {

	private static final long serialVersionUID = 7439642984069939024L;

	public InvalidCredentialsException(@NonNull final String message) {
		super(HttpStatus.UNAUTHORIZED, message);
	}

}