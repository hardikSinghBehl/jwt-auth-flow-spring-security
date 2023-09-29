package com.behl.cerberus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.RefreshTokenRequestDto;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.service.AuthenticationService;

class AuthenticationControllerTest {

	private AuthenticationController authenticationController;
	private AuthenticationService authenticationService;

	@BeforeEach
	void setUp() {
		this.authenticationService = mock(AuthenticationService.class);
		this.authenticationController = new AuthenticationController(authenticationService);
	}

	@Test
	void loginSuccess() {
		// Prepare
		var userLoginRequestDto = mock(UserLoginRequestDto.class);
		var tokenSuccessResponseDto = mock(TokenSuccessResponseDto.class);
		when(authenticationService.login(userLoginRequestDto)).thenReturn(tokenSuccessResponseDto);

		// Call
		final var response = authenticationController.login(userLoginRequestDto);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getBody()).isEqualTo(tokenSuccessResponseDto);
		verify(authenticationService, times(1)).login(userLoginRequestDto);
	}

	@Test
	void invalidCredentialsGivenDuringLogin() {
		// Prepare
		final String errorMessage = "Invalid login credentials provided";
		var userLoginRequestDto = mock(UserLoginRequestDto.class);
		when(authenticationService.login(userLoginRequestDto))
				.thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, errorMessage));

		// Call and Verify
		final var response = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationController.login(userLoginRequestDto));
		assertThat(response.getMessage()).contains(errorMessage);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(authenticationService).login(userLoginRequestDto);
	}

	@Test
	void refreshTokenSuccess() {
		// Prepare
		var refreshTokenRequestDto = mock(RefreshTokenRequestDto.class);
		var tokenSuccessResponseDto = mock(TokenSuccessResponseDto.class);
		when(authenticationService.refreshToken(refreshTokenRequestDto)).thenReturn(tokenSuccessResponseDto);

		// Call
		final var response = authenticationController.refreshToken(refreshTokenRequestDto);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getBody()).isEqualTo(tokenSuccessResponseDto);
		verify(authenticationService, times(1)).refreshToken(refreshTokenRequestDto);
	}

	@Test
	void refreshTokenExpired() {
		// Prepare
		final String errorMessage = "Token expired";
		var refreshTokenRequestDto = mock(RefreshTokenRequestDto.class);
		when(authenticationService.refreshToken(refreshTokenRequestDto))
				.thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage));

		// Call and Verify
		final var response = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationController.refreshToken(refreshTokenRequestDto));
		assertThat(response.getMessage()).contains(errorMessage);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		verify(authenticationService, times(1)).refreshToken(refreshTokenRequestDto);
	}

}
