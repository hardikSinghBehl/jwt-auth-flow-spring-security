package com.behl.cerberus.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.behl.cerberus.dto.IdentityVerificationRequestDto;
import com.behl.cerberus.entity.ResidentialAddress;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.entity.UserStatus;
import com.behl.cerberus.repository.ResidentialAddressRepository;
import com.behl.cerberus.repository.UserRepository;

class IdentityVerificationServiceTest {
	
	private final UserRepository userRepository = mock(UserRepository.class);
	private final ResidentialAddressRepository residentialAddressRepository = mock(ResidentialAddressRepository.class);
	private final IdentityVerificationService identityVerificationService = new IdentityVerificationService(userRepository, residentialAddressRepository);
	
	@Test
	void shouldVerifyUserIdentityAndUpdateUserStatusAndResidentialAddress() {
		// prepare identity verification request
		final var identityVerificationRequest = mock(IdentityVerificationRequestDto.class);

		// set up datasource to return user entity corresponding to userId
		final var userId = UUID.randomUUID();
		final var user = mock(User.class);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		
		// Invoke method under test
		identityVerificationService.verifyUserIdentity(userId, identityVerificationRequest);

		// Verify mock interactions
		verify(userRepository).findById(userId);
		verify(user).setUserStatus(UserStatus.APPROVED);
		verify(userRepository).save(user);
		verify(residentialAddressRepository).save(any(ResidentialAddress.class));
	}
	
}
