package com.behl.cerberus.service;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.RefreshTokenRequestDto;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.repository.UserRepository;
import com.behl.cerberus.security.utility.JwtUtility;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtility jwtUtility;

	public TokenSuccessResponseDto login(final UserLoginRequestDto userLoginRequestDto) {
		final var user = userRepository.findByEmailId(userLoginRequestDto.getEmailId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials provided"));

		if (!passwordEncoder.matches(userLoginRequestDto.getPassword(), user.getPassword()))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login credentials provided");

		final var accessToken = jwtUtility.generateAccessToken(user);
		final var refreshToken = jwtUtility.generateRefreshToken(user);
		final var accessTokenExpirationTimestamp = jwtUtility.extractExpirationTimestamp(accessToken);

		return TokenSuccessResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken)
				.expiresAt(accessTokenExpirationTimestamp).build();
	}

	public TokenSuccessResponseDto refreshToken(final RefreshTokenRequestDto refreshTokenRequestDto) {

		if (jwtUtility.isTokenExpired(refreshTokenRequestDto.getRefreshToken()))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token expired");

		final UUID userId = jwtUtility.extractUserId(refreshTokenRequestDto.getRefreshToken());
		final var user = userRepository.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user-id provided"));

		final var accessToken = jwtUtility.generateAccessToken(user);
		final var accessTokenExpirationTimestamp = jwtUtility.extractExpirationTimestamp(accessToken);

		return TokenSuccessResponseDto.builder().accessToken(accessToken).expiresAt(accessTokenExpirationTimestamp)
				.build();
	}

}
