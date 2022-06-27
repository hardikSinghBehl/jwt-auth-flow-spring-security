package com.behl.cerberus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.cerberus.dto.UserCreationRequestDto;

@RestController
@RequestMapping(value = "/users")
public class UserController {

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.CREATED)
	public ResponseEntity<HttpStatus> userCreationHandler(
			@RequestBody(required = true) final UserCreationRequestDto userCreationRequestDto) {
		return null;
	}

}
