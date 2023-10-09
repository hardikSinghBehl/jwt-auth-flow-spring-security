package com.behl.cerberus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InsufficientBalanceException extends ResponseStatusException {

	private static final long serialVersionUID = 5140628759691601391L;
	private static final String DEFAULT_MESSAGE = "Insufficient balance to perform transaction";

	public InsufficientBalanceException() {
		super(HttpStatus.NOT_ACCEPTABLE, DEFAULT_MESSAGE);
	}

}