package com.behl.cerberus.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.dto.UserUpdationRequestDto;
import com.behl.cerberus.entity.UserStatus;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.exception.ExceptionResponseHandler;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.service.UserService;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;
import com.behl.cerberus.utility.JwtUtility;

import io.swagger.v3.core.util.Json;
import lombok.SneakyThrows;

@WebMvcTest(controllers = UserController.class)
@Import({ ExceptionResponseHandler.class, SecurityConfiguration.class, CustomAuthenticationEntryPoint.class })
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@SpyBean
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;

	@MockBean
	private JwtUtility jwtUtility;

	@MockBean
	private TokenRevocationService tokenRevocationService;
	
	@Test
	@SneakyThrows
	void userCreationShouldThrowExceptionForDuplicateEmailId() {
		// prepare user creation request
		final var userCreationRequest = new UserCreationRequestDto();
		userCreationRequest.setEmailId("mail@domain.ut");
		userCreationRequest.setPassword("test-password");
		userCreationRequest.setFirstName("test-first-name");
		userCreationRequest.setLastName("test-last-name");

		// mock service layer to throw AccountAlreadyExistsException
		doThrow(new AccountAlreadyExistsException("Account with provided email-id already exists")).when(userService).create(refEq(userCreationRequest));

		// execute API request
		final var apiPath = "/users";
		final var requestBody = Json.mapper().writeValueAsString(userCreationRequest);
		mockMvc.perform(post(apiPath)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isConflict())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.CONFLICT.toString()))
				.andExpect(jsonPath("$.Description").value("Account with provided email-id already exists"));

		// verify mock interaction
		verify(userService).create(refEq(userCreationRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldCreateUserEntityForValidUserCreationRequest() {
		// prepare user creation request
		final var userCreationRequest = new UserCreationRequestDto();
		userCreationRequest.setEmailId("mail@domain.ut");
		userCreationRequest.setPassword("test-password");
		userCreationRequest.setFirstName("test-first-name");
		userCreationRequest.setLastName("test-last-name");

		// mock service layer invocation
		doNothing().when(userService).create(refEq(userCreationRequest));

		// execute API request
		final var apiPath = "/users";
		final var requestBody = Json.mapper().writeValueAsString(userCreationRequest);
		mockMvc.perform(post(apiPath)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isCreated())
				.andDo(print());

		// verify mock interaction
		verify(userService).create(refEq(userCreationRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldThrowBadRequestForMissingUserCreationRequestDetails() {
		// prepare user creation request
		final var userCreationRequest = "{}";

		// execute API request
		final var apiPath = "/users";
		mockMvc.perform(post(apiPath)
				.contentType(MediaType.APPLICATION_JSON)
				.content(userCreationRequest))
				.andExpect(status().isBadRequest())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.BAD_REQUEST.toString()))
				.andExpect(jsonPath("$.Description").isArray())
				.andExpect(jsonPath("$.Description")
					    .value(Matchers.containsInAnyOrder(
					    	"first-name must not be empty",
					    	"email-id must not be empty",
					    	"password must not be empty"
					    )));

		// verify mock interaction
		verify(userService, times(0)).create(any(UserCreationRequestDto.class));
	}
	
	@Test
	@SneakyThrows
	void shouldRetreiveProfileDetailsForLoggedInUser() {
		// simulate authority extraction from access token
		final var scope = "userprofile.read";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// Prepare user profile details
		final var firstName = "test-first-name";
		final var lastName = "test-last-name";
		final var emailId = "mail@domain.ut";
		final var userStatus = UserStatus.APPROVED.getValue();
		final var dateOfBirth = LocalDate.now();
		final var createdAt = LocalDateTime.now();
		final var userDetail = mock(UserDetailDto.class);
		
		when(userDetail.getFirstName()).thenReturn(firstName);
		when(userDetail.getLastName()).thenReturn(lastName);
		when(userDetail.getEmailId()).thenReturn(emailId);
		when(userDetail.getStatus()).thenReturn(userStatus);
		when(userDetail.getDateOfBirth()).thenReturn(dateOfBirth);
		when(userDetail.getCreatedAt()).thenReturn(createdAt);
		
		when(userService.getById(userId)).thenReturn(userDetail);
		
		// execute API request
		final var apiPath = "/users";
		mockMvc.perform(get(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$.FirstName").value(firstName))
				.andExpect(jsonPath("$.LastName").value(lastName))
				.andExpect(jsonPath("$.EmailId").value(emailId))
				.andExpect(jsonPath("$.Status").value(userStatus))
				.andExpect(jsonPath("$.DateOfBirth").value(dateOfBirth.toString()))
				.andExpect(jsonPath("$.CreatedAt").value(createdAt.toString()));
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(userService).getById(userId);
	}
	
	@Test
	@SneakyThrows
	void shouldSuccessfullyUpdateProfileDetailsForLoggedInUser() {
		// prepare user updation request
		final var userUpdationRequest = new UserUpdationRequestDto();
		userUpdationRequest.setFirstName("test-first-name");
		userUpdationRequest.setLastName("test-last-name");
		
		// simulate authority extraction from access token
		final var scope = "userprofile.update";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// execute API request
		final var apiPath = "/users";
		final var requestBody = Json.mapper().writeValueAsString(userUpdationRequest);
		mockMvc.perform(put(apiPath)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isOk())
				.andDo(print());

		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(userService).update(eq(userId), refEq(userUpdationRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldDeactivateLoggedInUser() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// execute API request
		final var apiPath = "/users/deactivate";
		mockMvc.perform(delete(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNoContent())
				.andDo(print());
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(userService).deactivate(userId);
	}

}
