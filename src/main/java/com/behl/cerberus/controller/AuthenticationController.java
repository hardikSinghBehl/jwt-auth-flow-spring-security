package com.behl.cerberus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.behl.cerberus.dto.RefreshTokenRequestDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.dto.TokenSuccessResponseDto;

@RestController
@RequestMapping(value = "/auth")
public class AuthenticationController {

	@PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<TokenSuccessResponseDto> userLoginRequestHandler(
			@RequestBody(required = true) final UserLoginRequestDto userLoginRequestDto) {
		return null;
	}

	@PutMapping(value = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<TokenSuccessResponseDto> accessTokenRefreshalRequestHandler(
			@RequestBody(required = true) final RefreshTokenRequestDto refreshTokenRequestDto) {
		return null;
	}
}
