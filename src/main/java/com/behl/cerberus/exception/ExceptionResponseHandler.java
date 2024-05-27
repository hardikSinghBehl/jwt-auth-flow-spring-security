package com.behl.cerberus.exception;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.behl.cerberus.dto.ExceptionResponseDto;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class ExceptionResponseHandler extends ResponseEntityExceptionHandler {
	
	private static final String FORBIDDEN_ERROR_MESSAGE = "Access Denied: You do not have sufficient privileges to access this resource.";
	private static final String NOT_READABLE_REQUEST_ERROR_MESSAGE = "The request is malformed. Ensure the JSON structure is correct.";
	
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ExceptionResponseDto<String>> responseStatusExceptionHandler(final ResponseStatusException exception) {
		logException(exception);
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(exception.getStatusCode().toString());
		exceptionResponse.setDescription(exception.getReason());
		return ResponseEntity.status(exception.getStatusCode()).body(exceptionResponse);
	}
	
	@ExceptionHandler(CompromisedPasswordException.class)
	public ResponseEntity<ExceptionResponseDto<String>> compromisedPasswordExceptionHandler(final CompromisedPasswordException exception) {
		logException(exception);
		final var statusCode = HttpStatus.UNPROCESSABLE_ENTITY;
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(statusCode.toString());
		exceptionResponse.setDescription(exception.getMessage());
		return ResponseEntity.status(statusCode).body(exceptionResponse);
	}
	
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ExceptionResponseDto<String>> accessDeniedExceptionHandler(final AccessDeniedException exception) {
		logException(exception);
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(HttpStatus.FORBIDDEN.toString());
		exceptionResponse.setDescription(FORBIDDEN_ERROR_MESSAGE);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		logException(exception);
		final var fieldErrors = exception.getBindingResult().getFieldErrors();
		final var description = fieldErrors.stream().map(fieldError -> fieldError.getDefaultMessage()).collect(Collectors.toList());
		
		final var exceptionResponse = new ExceptionResponseDto<List<String>>();
		exceptionResponse.setStatus(HttpStatus.BAD_REQUEST.toString());
		exceptionResponse.setDescription(description);

		return ResponseEntity.badRequest().body(exceptionResponse);
	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		logException(exception);
		final var exceptionResponse = new ExceptionResponseDto<AtomicReference<String>>();
		exceptionResponse.setStatus(HttpStatus.BAD_REQUEST.toString());
		final var description = new AtomicReference<String>(NOT_READABLE_REQUEST_ERROR_MESSAGE);

		if (exception.getCause() instanceof InvalidFormatException invalidFormatException) {
			invalidFormatException.getPath().stream().map(Reference::getFieldName).findFirst().ifPresent(fieldName -> {
				final var invalidValue = invalidFormatException.getValue();
				final var errorMessage = String.format("Invalid value '%s' for '%s'.", invalidValue, fieldName);
				description.set(errorMessage);
			});
		} else if (exception.getCause() instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
			unrecognizedPropertyException.getPath().stream().map(Reference::getFieldName).findFirst().ifPresent(fieldName -> {
				final var errorMessage = String.format("Unrecognized property '%s' detected.", fieldName);
				description.set(errorMessage);
			});
		} else if (exception.getCause() instanceof MismatchedInputException mismatchedInputException) {
			mismatchedInputException.getPath().stream().map(Reference::getFieldName).findFirst().ifPresent(fieldName -> {
				final var errorMessage = String.format("Invalid data type for field '%s'.", fieldName);
				description.set(errorMessage);
			});
		}

		exceptionResponse.setDescription(description);
		return ResponseEntity.badRequest().body(exceptionResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> serverExceptionHandler(final Exception exception) {
		logException(exception);
		final var exceptionResponse = new ExceptionResponseDto<String>();
		exceptionResponse.setStatus(HttpStatus.NOT_IMPLEMENTED.toString());
		exceptionResponse.setDescription("Something went wrong.");
		return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(exceptionResponse);
	}
	
	private void logException(final @NonNull Exception exception) {
		log.error("Exception encountered: {}", exception.getMessage(), exception);
	}

}