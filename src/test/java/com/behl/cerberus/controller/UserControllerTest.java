package com.behl.cerberus.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.service.UserService;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;

class UserControllerTest {

	private UserService userService;
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;
	
	private UserController userController;

	@BeforeEach
	void setUp() {
		this.userService = mock(UserService.class);
		this.authenticatedUserIdProvider = mock(AuthenticatedUserIdProvider.class);
		this.userController = new UserController(userService, authenticatedUserIdProvider);
	}

	@Test
	void userCreationSuccess() {
		// Prepare
		var userCreationRequestDto = mock(UserCreationRequestDto.class);
		doNothing().when(userService).create(userCreationRequestDto);

		// Call
		final var response = userController.createUser(userCreationRequestDto);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		verify(userService, times(1)).create(userCreationRequestDto);
	}

	@Test
	void userCreationWithUsedEmailId() {
		// Prepare
		final String errorMessage = "Account with provided email-id already exists";
		var userCreationRequestDto = mock(UserCreationRequestDto.class);
		doThrow(new ResponseStatusException(HttpStatus.CONFLICT, errorMessage)).when(userService)
				.create(userCreationRequestDto);

		// Call and Verify
		final var response = Assertions.assertThrows(ResponseStatusException.class,
				() -> userController.createUser(userCreationRequestDto));
		assertThat(response.getMessage()).contains(errorMessage);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		verify(userService).create(userCreationRequestDto);
	}

	@Test
	void getUserDetailsWithAccessToken() {
		// Prepare
		final UUID userId = UUID.fromString("304227a6-5938-4bbc-9c3c-a13520372abc");
		var userDetailDto = mock(UserDetailDto.class);
		when(authenticatedUserIdProvider.getUserId()).thenReturn(userId);
		when(userService.getById(userId)).thenReturn(userDetailDto);

		// Call
		final var response = userController.retrieveUser();

		// Verify
		assertThat(response).isNotNull();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody()).isInstanceOf(UserDetailDto.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		verify(authenticatedUserIdProvider, times(1)).getUserId();
		verify(userService, times(1)).getById(userId);
	}

}
