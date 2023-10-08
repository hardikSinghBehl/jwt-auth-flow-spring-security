package com.behl.cerberus.service;

import java.util.UUID;

import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.behl.cerberus.configuration.DatasourcePatchOperationManager;
import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.dto.UserUpdationRequestDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void create(@NonNull final UserCreationRequestDto userCreationRequestDto) {
		final var emailId = userCreationRequestDto.getEmailId();
		final var userAccountExistsWithEmailId = userRepository.existsByEmailId(emailId);
		if (Boolean.TRUE.equals(userAccountExistsWithEmailId)) {
			throw new AccountAlreadyExistsException("Account with provided email-id already exists");
		}
		
		final var user = new User();
		final var encodedPassword = passwordEncoder.encode(userCreationRequestDto.getPassword());
		user.setFirstName(userCreationRequestDto.getFirstName());
		user.setLastName(userCreationRequestDto.getLastName());
		user.setEmailId(userCreationRequestDto.getEmailId());
		user.setPassword(encodedPassword);

		userRepository.save(user);
	}

	public void update(@NonNull final UUID userId, @NonNull UserUpdationRequestDto userUpdationRequestDto) {
		final var user = getUserById(userId);
		Mappers.getMapper(DatasourcePatchOperationManager.class).patch(userUpdationRequestDto, user);
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

	private User getUserById(@NonNull final UUID userId) {
		return userRepository.findById(userId).orElseThrow(IllegalStateException::new);
	}

}