package com.behl.cerberus.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.behl.cerberus.configuration.CustomAuthenticationEntryPoint;
import com.behl.cerberus.configuration.SecurityConfiguration;
import com.behl.cerberus.exception.ExceptionResponseHandler;
import com.behl.cerberus.service.DepositAccountService;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;
import com.behl.cerberus.utility.JwtUtility;

import lombok.SneakyThrows;

@WebMvcTest(controllers = DepositAccountController.class)
@Import({ ExceptionResponseHandler.class, SecurityConfiguration.class, CustomAuthenticationEntryPoint.class, ApiEndpointSecurityInspector.class })
class InsufficientScopesControllerTest {
	
	@Autowired
    private MockMvc mockMvc;

	@MockBean
	private DepositAccountService depositAccountService;

	@MockBean
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;

	@MockBean
	private JwtUtility jwtUtility;

	@MockBean
	private TokenRevocationService tokenRevocationService;
	
	@Test
	@SneakyThrows
	@DisplayName("API should return 403 Forbidden if required scopes not present in access token")
	void shouldReturnForbiddenIfAccessTokenLacksRequiredPermission() {
		// Simulate the absence of required scopes in access token
		final var scope = "not:fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// Send request to an API that requires "fullaccess" scope
		final var apiPath = "/deposit-accounts";
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isForbidden())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.FORBIDDEN.toString()))
				.andExpect(jsonPath("$.Description").value("Access Denied: You do not have sufficient privileges to access this resource."));
	}

}
