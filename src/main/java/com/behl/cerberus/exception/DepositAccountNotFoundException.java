package com.behl.cerberus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class DepositAccountNotFoundException extends ResponseStatusException {

	private static final long serialVersionUID = 2636579209666349410L;
	private static final String DEFAULT_MESSAGE = "Deposit account must be created prior to performing this operation";

	public DepositAccountNotFoundException() {
		super(HttpStatus.NOT_FOUND, DEFAULT_MESSAGE);
	}

}