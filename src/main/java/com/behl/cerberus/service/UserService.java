package com.behl.cerberus.service;

import java.util.UUID;

import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.dto.UserUpdationRequestDto;
import com.behl.cerberus.dto.mapper.UserPatchOperationMapper;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.repository.UserRepository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void create(@NonNull final UserCreationRequestDto userCreationRequestDto) {
		if (emailAlreadyTaken(userCreationRequestDto.getEmailId()))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Account with provided email-id already exists");

		final var user = new User();
		user.setFirstName(userCreationRequestDto.getFirstName());
		user.setLastName(userCreationRequestDto.getLastName());
		user.setEmailId(userCreationRequestDto.getEmailId());
		user.setPassword(passwordEncoder.encode(userCreationRequestDto.getPassword()));

		userRepository.save(user);
	}

	public void update(@NonNull final UUID userId, @NonNull UserUpdationRequestDto userUpdationRequestDto) {
		final var user = getUserById(userId);
		Mappers.getMapper(UserPatchOperationMapper.class).patch(userUpdationRequestDto, user);
		userRepository.save(user);
	}

	public UserDetailDto getById(@NonNull final UUID userId) {
		final var user = getUserById(userId);
		return UserDetailDto.builder().firstName(user.getFirstName()).lastName(user.getLastName())
				.emailId(user.getEmailId()).createdAt(user.getCreatedAt()).build();
	}

	private User getUserById(@NonNull final UUID userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user-id provided"));
	}

	private boolean emailAlreadyTaken(@NonNull final String emailId) {
		return userRepository.existsByEmailId(emailId);
	}

}