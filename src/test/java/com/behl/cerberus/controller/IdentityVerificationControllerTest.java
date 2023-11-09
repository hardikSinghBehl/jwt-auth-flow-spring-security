package com.behl.cerberus.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.behl.cerberus.configuration.CustomAuthenticationEntryPoint;
import com.behl.cerberus.configuration.SecurityConfiguration;
import com.behl.cerberus.dto.IdentityVerificationRequestDto;
import com.behl.cerberus.exception.ExceptionResponseHandler;
import com.behl.cerberus.service.IdentityVerificationService;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;
import com.behl.cerberus.utility.JwtUtility;

import io.swagger.v3.core.util.Json;
import lombok.SneakyThrows;

@WebMvcTest(controllers = IdentityVerificationController.class)
@Import({ ExceptionResponseHandler.class, SecurityConfiguration.class, CustomAuthenticationEntryPoint.class, ApiEndpointSecurityInspector.class })
class IdentityVerificationControllerTest {
	
	@Autowired
    private MockMvc mockMvc;

	@MockBean
	private IdentityVerificationService identityVerificationService;

	@SpyBean
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;

	@MockBean
	private JwtUtility jwtUtility;

	@MockBean
	private TokenRevocationService tokenRevocationService;
	
	@Test
	@SneakyThrows
	void shouldVerifyIdentityForValidRequest() {		
		// prepare identity verification request
		final var identityVerificationRequest = new IdentityVerificationRequestDto();
		identityVerificationRequest.setDateOfBirth(LocalDate.now().minusDays(1l));
		identityVerificationRequest.setState("test-residential-state");
		identityVerificationRequest.setCity("test-residential-city");
		identityVerificationRequest.setPostalCode("123456");
		identityVerificationRequest.setStreetAddress("test-residential-street");

		// simulate authority extraction from access token
		final var scope = "useridentity.verify";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// execute API request
		final var apiPath = "/users/identity-verification";
		final var requestBody = Json.mapper().writeValueAsString(identityVerificationRequest);
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isOk())
				.andDo(print());
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(identityVerificationService).verifyUserIdentity(eq(userId), refEq(identityVerificationRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldThrowBadRequestForMissingIdentityVerificationDetails() {
		// prepare empty request body
		final var identityVerificationRequest = "{}";

		// execute API request
		final var apiPath = "/users/identity-verification";
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer test-access-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(identityVerificationRequest))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.BAD_REQUEST.toString()))
				.andExpect(jsonPath("$.Description").isArray())
				.andExpect(jsonPath("$.Description")
					    .value(Matchers.containsInAnyOrder(
					    	"Date of birth must not be empty",
					    	"State must not be empty",
					    	"City must not be empty",
					    	"Postal code must not be empty",
					    	"Street address must not be empty"
					    )));

		// verify mock interaction
		verify(identityVerificationService, times(0)).verifyUserIdentity(any(UUID.class), any(IdentityVerificationRequestDto.class));
	}

}
