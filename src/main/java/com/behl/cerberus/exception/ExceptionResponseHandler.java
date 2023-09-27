package com.behl.cerberus.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.behl.cerberus.dto.ExceptionResponseDto;

@ControllerAdvice
public class ExceptionResponseHandler extends ResponseEntityExceptionHandler {

	@ResponseBody
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<?> responseStatusExceptionHandler(final ResponseStatusException exception) {
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(exception.getStatusCode().toString());
		exceptionResponse.setDescription(exception.getReason());
		return ResponseEntity.status(exception.getStatusCode()).body(exceptionResponse);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		final var fieldErrors = exception.getBindingResult().getFieldErrors();
		final var description = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage()).collect(Collectors.toList());
		
		final var exceptionResponse = new ExceptionResponseDto<List<String>>();
		exceptionResponse.setStatus(HttpStatus.BAD_REQUEST.toString());
		exceptionResponse.setDescription(description);

		return ResponseEntity.badRequest().body(exceptionResponse);
	}

	@ResponseBody
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> serverExceptionHandler(final Exception exception) {
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(HttpStatus.NOT_IMPLEMENTED.toString());
		exceptionResponse.setDescription("Something went wrong.");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(exceptionResponse);
	}

}