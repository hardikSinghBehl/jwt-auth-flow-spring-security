package com.behl.cerberus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.service.AuthenticationService;
import com.behl.cerberus.utility.RefreshTokenHeaderProvider;

import net.bytebuddy.utility.RandomString;

class AuthenticationControllerTest {

	private AuthenticationController authenticationController;
	private AuthenticationService authenticationService;
	private RefreshTokenHeaderProvider refreshTokenHeaderProvider;

	@BeforeEach
	void setUp() {
		this.authenticationService = mock(AuthenticationService.class);
		this.refreshTokenHeaderProvider = mock(RefreshTokenHeaderProvider.class);
		this.authenticationController = new AuthenticationController(authenticationService, refreshTokenHeaderProvider);
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
		final var refreshToken = RandomString.make();
		var tokenSuccessResponseDto = mock(TokenSuccessResponseDto.class);
		when(refreshTokenHeaderProvider.getRefreshToken()).thenReturn(Optional.of(refreshToken));
		when(authenticationService.refreshToken(refreshToken)).thenReturn(tokenSuccessResponseDto);

		// Call
		final var response = authenticationController.refreshToken();

		// Verify
		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getBody()).isEqualTo(tokenSuccessResponseDto);
		verify(authenticationService, times(1)).refreshToken(refreshToken);
	}

	@Test
	void refreshTokenExpired() {
		// Prepare
		final String errorMessage = "Token expired";
		final var refreshToken = RandomString.make();
		when(refreshTokenHeaderProvider.getRefreshToken()).thenReturn(Optional.of(refreshToken));
		when(authenticationService.refreshToken(refreshToken))
				.thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage));

		// Call and Verify
		final var response = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationController.refreshToken());
		assertThat(response.getMessage()).contains(errorMessage);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		verify(authenticationService, times(1)).refreshToken(refreshToken);
	}

}
