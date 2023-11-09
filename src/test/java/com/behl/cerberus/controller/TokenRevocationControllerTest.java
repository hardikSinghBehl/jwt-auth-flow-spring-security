package com.behl.cerberus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import com.behl.cerberus.configuration.CustomAuthenticationEntryPoint;
import com.behl.cerberus.configuration.SecurityConfiguration;
import com.behl.cerberus.exception.ExceptionResponseHandler;
import com.behl.cerberus.exception.TokenVerificationException;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.service.UserService;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;
import com.behl.cerberus.utility.JwtUtility;

import lombok.SneakyThrows;

@WebMvcTest(controllers = UserController.class)
@Import({ ExceptionResponseHandler.class, SecurityConfiguration.class, CustomAuthenticationEntryPoint.class, ApiEndpointSecurityInspector.class })
class TokenRevocationControllerTest {
	
	@Autowired
    private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;

	@MockBean
	private JwtUtility jwtUtility;

	@MockBean
	private TokenRevocationService tokenRevocationService;
	
	@Test
	@SneakyThrows
	void shouldNotAllowAccessToSecuredApiIfAccessTokenRevoked() {
		// mock access token revocation
		final var accessToken = "test-revoked-access-token";
		when(tokenRevocationService.isRevoked(accessToken)).thenReturn(Boolean.TRUE);
		
		// execute API request
		final var exception = assertThrows(TokenVerificationException.class, () -> {
			final var apiPath = "/users";
			mockMvc.perform(get(apiPath)
					.header("Authorization", "Bearer " + accessToken));
		});
		
		// assert exception details
		assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(exception.getReason()).isEqualTo("Authentication failure: Token missing, invalid, revoked or expired");
		
		// verify mock interaction
		verify(tokenRevocationService).isRevoked(accessToken);
	}

}
