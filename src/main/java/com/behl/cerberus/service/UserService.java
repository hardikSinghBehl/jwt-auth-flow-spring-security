package com.behl.cerberus.service;

import java.util.UUID;

import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.dto.UserUpdationRequestDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.entity.UserStatus;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenRevocationService tokenRevocationService;
	private final CompromisedPasswordChecker compromisedPasswordChecker;

	public void create(@NonNull final UserCreationRequestDto userCreationRequest) {
		final var emailId = userCreationRequest.getEmailId();
		final var userAccountExistsWithEmailId = userRepository.existsByEmailId(emailId);
		if (Boolean.TRUE.equals(userAccountExistsWithEmailId)) {
			throw new AccountAlreadyExistsException("Account with provided email-id already exists");
		}

		final var plainTextPassword = userCreationRequest.getPassword();
		final var isPasswordCompromised = compromisedPasswordChecker.check(plainTextPassword).isCompromised();
		if (Boolean.TRUE.equals(isPasswordCompromised)) {
			throw new CompromisedPasswordException("The provided password is compromised and cannot be used for account creation.");
		}
		
		final var user = new User();
		final var encodedPassword = passwordEncoder.encode(plainTextPassword);
		user.setFirstName(userCreationRequest.getFirstName());
		user.setLastName(userCreationRequest.getLastName());
		user.setEmailId(userCreationRequest.getEmailId());
		user.setPassword(encodedPassword);

		userRepository.save(user);
	}

	public void update(@NonNull final UUID userId, @NonNull UserUpdationRequestDto userUpdationRequestDto) {
		final var user = getUserById(userId);
		user.setFirstName(userUpdationRequestDto.getFirstName());
		user.setLastName(userUpdationRequestDto.getLastName());
		userRepository.save(user);
	}

	public UserDetailDto getById(@NonNull final UUID userId) {
		final var user = getUserById(userId);
		return UserDetailDto.builder()
				.firstName(user.getFirstName())
				.lastName(user.getLastName())
				.emailId(user.getEmailId())
				.status(user.getUserStatus().getValue())
				.dateOfBirth(user.getDateOfBirth())
				.createdAt(user.getCreatedAt())
				.build();
	}
	
	public void deactivate(@NonNull final UUID userId) {
		final var user = getUserById(userId);
		user.setUserStatus(UserStatus.DEACTIVATED);
		userRepository.save(user);
		
		tokenRevocationService.revoke();
	}

	private User getUserById(@NonNull final UUID userId) {
		return userRepository.findById(userId).orElseThrow(IllegalStateException::new);
	}

}