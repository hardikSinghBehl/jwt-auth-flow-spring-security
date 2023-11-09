package com.behl.cerberus.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.behl.cerberus.configuration.CustomAuthenticationEntryPoint;
import com.behl.cerberus.configuration.SecurityConfiguration;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.exception.ExceptionResponseHandler;
import com.behl.cerberus.exception.InvalidLoginCredentialsException;
import com.behl.cerberus.exception.TokenVerificationException;
import com.behl.cerberus.service.AuthenticationService;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;
import com.behl.cerberus.utility.JwtUtility;
import com.behl.cerberus.utility.RefreshTokenHeaderProvider;

import io.swagger.v3.core.util.Json;
import lombok.SneakyThrows;

@WebMvcTest(controllers = AuthenticationController.class)
@Import({ ExceptionResponseHandler.class, SecurityConfiguration.class, CustomAuthenticationEntryPoint.class, ApiEndpointSecurityInspector.class })
class AuthenticationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuthenticationService authenticationService;

	@SpyBean
	private RefreshTokenHeaderProvider refreshTokenHeaderProvider;

	@MockBean
	private JwtUtility jwtUtility;

	@MockBean
	private TokenRevocationService tokenRevocationService;

	@Test
	@SneakyThrows
	void shouldReturnUnauthorizedAgainstInvalidLoginCredentials() {
		// prepare user login request
		final var userLoginRequest = new UserLoginRequestDto();
		userLoginRequest.setEmailId("mail@domain.ut");
		userLoginRequest.setPassword("test-password");

		// mock service layer to throw InvalidLoginCredentialsException
		when(authenticationService.login(refEq(userLoginRequest))).thenThrow(new InvalidLoginCredentialsException());

		// execute API request
		final var apiPath = "/auth/login";
		final var requestBody = Json.mapper().writeValueAsString(userLoginRequest);
		mockMvc.perform(post(apiPath)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isUnauthorized())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
				.andExpect(jsonPath("$.Description").value("Invalid login credentials provided"));

		// verify mock interaction
		verify(authenticationService).login(refEq(userLoginRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldReturnAccessTokenForValidLoginRequest() {
		// prepare user login request
		final var userLoginRequest = new UserLoginRequestDto();
		userLoginRequest.setEmailId("mail@domain.ut");
		userLoginRequest.setPassword("test-password");
		
		// prepare service layer success response
		final var accessToken = "test-access-token";
		final var refreshToken = "test-refresh-token";
		final var tokenResponse = mock(TokenSuccessResponseDto.class);
		when(tokenResponse.getAccessToken()).thenReturn(accessToken);
		when(tokenResponse.getRefreshToken()).thenReturn(refreshToken);
		when(authenticationService.login(refEq(userLoginRequest))).thenReturn(tokenResponse);

		// execute API request
		final var apiPath = "/auth/login";
		final var requestBody = Json.mapper().writeValueAsString(userLoginRequest);
		mockMvc.perform(post(apiPath)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$.AccessToken").value(accessToken))
				.andExpect(jsonPath("$.RefreshToken").value(refreshToken));

		// verify mock interaction
		verify(authenticationService).login(refEq(userLoginRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldThrowBadRequestForMissingLoginDetails() {
		// prepare incomplete user login request
		final var userLoginRequest = new UserLoginRequestDto();
		userLoginRequest.setEmailId("mail@domain.ut");

		// execute API request
		final var apiPath = "/auth/login";
		final var requestBody = Json.mapper().writeValueAsString(userLoginRequest);
		mockMvc.perform(post(apiPath)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.BAD_REQUEST.toString()))
				.andExpect(jsonPath("$.Description").isArray())
				.andExpect(jsonPath("$.Description").value("password must not be empty"));

		// verify mock interaction
		verify(authenticationService, times(0)).login(any(UserLoginRequestDto.class));
	}
	
	@Test
	@SneakyThrows
	void shouldThrowExceptionIfRefreshTokenHeaderNotPresentInRequestHeader() {
		// execute API request without refresh token header
		final var apiPath = "/auth/refresh";
		mockMvc.perform(put(apiPath))
				.andExpect(status().isUnauthorized())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
				.andExpect(jsonPath("$.Description").value("Authentication failure: Token missing, invalid, revoked or expired"));
	}
	
	@Test
	@SneakyThrows
	void shouldThrowExceptionIfEmptyRefreshTokenHeaderPresentInRequestHeader() {		
		// execute API request with empty refresh token header value
		final var apiPath = "/auth/refresh";
		mockMvc.perform(put(apiPath)
				.header("X-Refresh-Token", StringUtils.EMPTY))
				.andExpect(status().isUnauthorized())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
				.andExpect(jsonPath("$.Description").value("Authentication failure: Token missing, invalid, revoked or expired"));
	}
	
	@Test
	@SneakyThrows
	void shouldReturnAccessTokenForValidRefreshToken() {		
		// prepare service layer success response
		final var accessToken = "test-access-token";
		final var refreshToken = "test-refresh-token";
		final var tokenResponse = mock(TokenSuccessResponseDto.class);
		when(tokenResponse.getAccessToken()).thenReturn(accessToken);
		when(authenticationService.refreshToken(refEq(refreshToken))).thenReturn(tokenResponse);
		
		// execute API request with refresh token header
		final var apiPath = "/auth/refresh";
		mockMvc.perform(put(apiPath)
				.header("X-Refresh-Token", refreshToken))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$.AccessToken").value(accessToken));
	}
	
	@Test
	@SneakyThrows
	void shouldReturnUnauthorizedForExpiredRefreshToken() {		
		// prepare service layer to throw expection for expired refresh token
		final var refreshToken = "test-expired-refresh-token";
		when(authenticationService.refreshToken(refEq(refreshToken))).thenThrow(new TokenVerificationException());
		
		// execute API request with refresh token header
		final var apiPath = "/auth/refresh";
		mockMvc.perform(put(apiPath)
				.header("X-Refresh-Token", refreshToken))
				.andExpect(status().isUnauthorized())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.UNAUTHORIZED.toString()))
				.andExpect(jsonPath("$.Description").value("Authentication failure: Token missing, invalid, revoked or expired"));
	}

}
