package com.behl.cerberus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TokenExpiredException extends ResponseStatusException {
	
	private static final long serialVersionUID = 7439642984069939024L;
	private static final String DEFAULT_MESSAGE = "Token expired";

	public TokenExpiredException() {
		super(HttpStatus.UNAUTHORIZED, DEFAULT_MESSAGE);
	}

}