package com.behl.cerberus.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.behl.cerberus.dto.RefreshTokenRequestDto;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.exception.InvalidLoginCredentialsException;
import com.behl.cerberus.exception.TokenExpiredException;
import com.behl.cerberus.repository.UserRepository;
import com.behl.cerberus.utility.JwtUtility;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtility jwtUtility;

	public TokenSuccessResponseDto login(@NonNull final UserLoginRequestDto userLoginRequestDto) {
		final var user = userRepository.findByEmailId(userLoginRequestDto.getEmailId())
				.orElseThrow(InvalidLoginCredentialsException::new);

		final var encodedPassword = user.getPassword();
		final var plainTextPassword = userLoginRequestDto.getPassword();
		final var isCorrectPassword = passwordEncoder.matches(plainTextPassword, encodedPassword);
		if (Boolean.FALSE.equals(isCorrectPassword)) {
			throw new InvalidLoginCredentialsException();
		}

		final var userId = user.getId();
		final var accessToken = jwtUtility.generateAccessToken(userId);
		final var refreshToken = jwtUtility.generateRefreshToken(userId);
		final var accessTokenExpirationTimestamp = jwtUtility.getExpirationTimestamp(accessToken);

		return TokenSuccessResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken)
				.expiresAt(accessTokenExpirationTimestamp).build();
	}

	public TokenSuccessResponseDto refreshToken(@NonNull final RefreshTokenRequestDto refreshTokenRequestDto) {
		final var isRefreshTokenExpired = jwtUtility.isTokenExpired(refreshTokenRequestDto.getRefreshToken());
		if (Boolean.TRUE.equals(isRefreshTokenExpired)) {
			throw new TokenExpiredException();
		}

		final var userId = jwtUtility.extractUserId(refreshTokenRequestDto.getRefreshToken());
		final var accessToken = jwtUtility.generateAccessToken(userId);
		final var accessTokenExpirationTimestamp = jwtUtility.getExpirationTimestamp(accessToken);

		return TokenSuccessResponseDto.builder().accessToken(accessToken).expiresAt(accessTokenExpirationTimestamp)
				.build();
	}

}
