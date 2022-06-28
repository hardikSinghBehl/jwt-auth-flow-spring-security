package com.behl.cerberus.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.UserCreationRequestDto;
import com.behl.cerberus.dto.UserDetailDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void create(final UserCreationRequestDto userCreationRequestDto) {
		if (emailAlreadyTaken(userCreationRequestDto.getEmailId()))
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Account with provided email-id already exists");

		final var user = new User();
		user.setFirstName(userCreationRequestDto.getFirstName());
		user.setLastName(userCreationRequestDto.getLastName());
		user.setEmailId(userCreationRequestDto.getEmailId());
		user.setPassword(passwordEncoder.encode(userCreationRequestDto.getPassword()));

		userRepository.save(user);
	}

	public UserDetailDto getById(final UUID userId) {
		final var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user-id provided"));
		return UserDetailDto.builder().firstName(user.getFirstName()).lastName(user.getLastName())
				.emailId(user.getEmailId()).createdAt(user.getCreatedAt()).build();
	}

	private boolean emailAlreadyTaken(final String emailId) {
		return userRepository.existsByEmailId(emailId);
	}

}
