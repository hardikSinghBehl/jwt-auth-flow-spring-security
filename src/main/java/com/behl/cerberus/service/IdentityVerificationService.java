package com.behl.cerberus.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.behl.cerberus.dto.IdentityVerificationRequestDto;
import com.behl.cerberus.entity.ResidentialAddress;
import com.behl.cerberus.entity.UserStatus;
import com.behl.cerberus.repository.ResidentialAddressRepository;
import com.behl.cerberus.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityVerificationService {
	
	private final UserRepository userRepository;
	private final ResidentialAddressRepository residentialAddressRepository;
	
	/**
	 * Verifies the identity of the user. In the context of this proof-of-concept,
	 * this method does not perform any actual identity verification checks. It
	 * simply updates the user's information and approves the user.
	 */
	public void verifyUserIdentity(@NonNull final UUID userId, @NonNull final IdentityVerificationRequestDto identityVerificationRequest) {		
		final var residentialAddress = new ResidentialAddress();
		residentialAddress.setUserId(userId);
		residentialAddress.setStreetAddress(identityVerificationRequest.getStreetAddress());
		residentialAddress.setCity(identityVerificationRequest.getCity());
		residentialAddress.setState(identityVerificationRequest.getState());
		residentialAddress.setPostalCode(identityVerificationRequest.getPostalCode());
		
		final var user = userRepository.findById(userId).orElseThrow(IllegalStateException::new);
		user.setDateOfBirth(identityVerificationRequest.getDateOfBirth());
		user.setUserStatus(UserStatus.APPROVED);
		
		residentialAddressRepository.save(residentialAddress);
		userRepository.save(user);
	}

}
