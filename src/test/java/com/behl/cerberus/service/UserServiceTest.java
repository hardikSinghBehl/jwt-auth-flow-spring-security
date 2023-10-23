package com.behl.cerberus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserUpdationRequestDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.entity.UserStatus;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.repository.UserRepository;

class UserServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final TokenRevocationService tokenRevocationService = mock(TokenRevocationService.class);
	private final UserService userService = new UserService(userRepository, passwordEncoder, tokenRevocationService);

	@Test
	void userCreationShouldThrowExceptionForDuplicateEmailId() {
		// prepare user creation request
		final var emailId = "duplicate@domain.ut";
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);

		// set datasource to evaluate duplicate emailid
		when(userRepository.existsByEmailId(emailId)).thenReturn(Boolean.TRUE);

		// invoke method under test and verify mock interactions
		final var exception = assertThrows(AccountAlreadyExistsException.class, () -> userService.create(userCreationRequest));
		assertThat(exception.getReason()).isEqualTo("Account with provided email-id already exists");
		verify(userRepository).existsByEmailId(emailId);
	}
	
	@Test
	void shouldCreateUserEntityForValidUserCreationRequest() {
		// prepare user creation request
		final var emailId = "mail@domain.ut";
		final var password = "test-password";
		final var firstName = "test-first-name";
		final var lastName = "test-last-name";
		final var userCreationRequest = mock(UserCreationRequestDto.class);
		when(userCreationRequest.getEmailId()).thenReturn(emailId);
		when(userCreationRequest.getPassword()).thenReturn(password);
		when(userCreationRequest.getFirstName()).thenReturn(firstName);
		when(userCreationRequest.getLastName()).thenReturn(lastName);

		// set datasource to evaluate non duplicate emailid
		when(userRepository.existsByEmailId(emailId)).thenReturn(Boolean.FALSE);
		
		// set password encoding
		final var encodedPassword = "test-encoded-password";
		when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
		
		// invoke method under test
		userService.create(userCreationRequest);

		// verify mock interactions
		verify(userRepository).existsByEmailId(emailId);
		verify(passwordEncoder).encode(password);
		verify(userRepository).save(any(User.class));
	}
	
	@Test
	void shouldUpdateUserForValidUserUpdationRequest() {
		// prepate user updation request
		final var firstName = "test-first-name";
		final var lastName = "test-last-name";
		final var userUpdationRequest = mock(UserUpdationRequestDto.class);
		when(userUpdationRequest.getFirstName()).thenReturn(firstName);
		when(userUpdationRequest.getLastName()).thenReturn(lastName);

		// prepare user entity
		final var userId = UUID.randomUUID();
		final var user = mock(User.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		// invoke method under test
		userService.update(userId, userUpdationRequest);

		// verify mock interactions
		verify(userRepository).findById(userId);
		verify(user).setFirstName(firstName);
		verify(user).setLastName(lastName);
		verify(userRepository).save(user);
	}
	
	@Test
	void shouldGetUserDetailForValidUserId() {
		// Prepare test data
		final var firstName = "test-first-name";
		final var lastName = "test-last-name";
		final var email = "mail@domain.ut";
		final var userStatus = UserStatus.APPROVED;
		final var dateOfBirth = LocalDate.now();
		final var createdAt = LocalDateTime.now();

		// Prepare user entity
		final var userId = UUID.randomUUID();
		final var user = mock(User.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(user.getFirstName()).thenReturn(firstName);
		when(user.getLastName()).thenReturn(lastName);
		when(user.getEmailId()).thenReturn(email);
		when(user.getUserStatus()).thenReturn(userStatus);
		when(user.getDateOfBirth()).thenReturn(dateOfBirth);
		when(user.getCreatedAt()).thenReturn(createdAt);

		// Invoke method under test
		final var response = userService.getById(userId);

		// verify response values and mock interaction
		assertThat(response.getFirstName()).isEqualTo(firstName);
		assertThat(response.getLastName()).isEqualTo(lastName);
		assertThat(response.getEmailId()).isEqualTo(email);
		assertThat(response.getStatus()).isEqualTo(userStatus.getValue());
		assertThat(response.getDateOfBirth()).isEqualTo(dateOfBirth);
		assertThat(response.getCreatedAt()).isEqualTo(createdAt);
		verify(userRepository).findById(userId);
	}
	
	@Test
	void shouldDeactivateUserAndRevokeTokenForValidUserId() {
		// prepare user entity
		final var userId = UUID.randomUUID();
		final var user = mock(User.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		
		// set token revocation call
		doNothing().when(tokenRevocationService).revoke();

		// invoke method under test
		userService.deactivate(userId);

		// verify mock interactions
		verify(userRepository).findById(userId);
		verify(user).setUserStatus(UserStatus.DEACTIVATED);
		verify(userRepository).save(user);
		verify(tokenRevocationService).revoke();
	}

}
