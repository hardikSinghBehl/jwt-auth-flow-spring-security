package com.behl.cerberus.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.behl.cerberus.configuration.JwtConfigurationProperties;
import com.behl.cerberus.dto.RefreshTokenRequestDto;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.exception.InvalidLoginCredentialsException;
import com.behl.cerberus.exception.TokenVerificationException;
import com.behl.cerberus.repository.UserRepository;
import com.behl.cerberus.utility.JwtUtility;
import com.behl.cerberus.utility.RefreshTokenGenerator;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtConfigurationProperties.class)
public class AuthenticationService {

	private final JwtUtility jwtUtility;
	private final CacheService cacheService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final RefreshTokenGenerator refreshTokenGenerator;
	private final JwtConfigurationProperties jwtConfigurationProperties;

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
		final var accessTokenExpirationTimestamp = jwtUtility.getExpirationTimestamp(accessToken);
		
		final var refreshToken = refreshTokenGenerator.generate();
		final var refreshTokenValidity = jwtConfigurationProperties.getJwt().getRefreshToken().getValidity();
		cacheService.save(refreshToken, userId, Duration.ofMinutes(refreshTokenValidity));

		return TokenSuccessResponseDto.builder().accessToken(accessToken).refreshToken(refreshToken)
				.expiresAt(accessTokenExpirationTimestamp).build();
	}

	public TokenSuccessResponseDto refreshToken(@NonNull final RefreshTokenRequestDto refreshTokenRequestDto) {
		final var refreshToken = refreshTokenRequestDto.getRefreshToken();
		final var userId = cacheService.fetch(refreshToken, UUID.class).orElseThrow(TokenVerificationException::new);

		final var accessToken = jwtUtility.generateAccessToken(userId);
		final var accessTokenExpirationTimestamp = jwtUtility.getExpirationTimestamp(accessToken);

		return TokenSuccessResponseDto.builder().accessToken(accessToken).expiresAt(accessTokenExpirationTimestamp)
				.build();
	}

}
