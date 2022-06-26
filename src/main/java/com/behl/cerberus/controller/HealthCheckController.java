package com.behl.cerberus.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;

@RestController
public class HealthCheckController {

	@GetMapping(value = "/ping", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Endpoint to check health of application")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<?> healthCheckEndpointHandler() {
		return ResponseEntity.ok(Map.of("message", "pong"));
	}

}
