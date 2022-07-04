package com.behl.cerberus.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.security.utility.JwtUtility;
import com.behl.cerberus.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;
	private final JwtUtility jwtUtility;

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Creates a user account", description = "Registers a unique user record in the system corresponding to the provided information")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "User account created successfully"),
			@ApiResponse(responseCode = "409", description = "User account with provided email-id already exists") })
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<HttpStatus> userCreationHandler(
			@Valid @RequestBody(required = true) final UserCreationRequestDto userCreationRequestDto) {
		userService.create(userCreationRequestDto);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Retrieves current logged-in user's account details", description = "Private endpoint which retreives user account details against the Access-token JWT provided in headers")
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<UserDetailDto> loggedInUserDetailRetreivalHandler(
			@Parameter(hidden = true) @RequestHeader(name = "Authorization", required = true) final String accessToken) {
		return ResponseEntity.ok(userService.getById(jwtUtility.extractUserId(accessToken)));
	}

}
