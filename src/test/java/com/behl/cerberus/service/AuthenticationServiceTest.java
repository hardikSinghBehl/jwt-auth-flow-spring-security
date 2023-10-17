package com.behl.cerberus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.configuration.TokenConfigurationProperties;
import com.behl.cerberus.configuration.TokenConfigurationProperties.RefreshToken;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.repository.UserRepository;
import com.behl.cerberus.utility.CacheManager;
import com.behl.cerberus.utility.JwtUtility;
import com.behl.cerberus.utility.RefreshTokenGenerator;

class AuthenticationServiceTest {

	private JwtUtility jwtUtility;
	private CacheManager cacheManager;
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private RefreshTokenGenerator refreshTokenGenerator;
	private TokenConfigurationProperties tokenConfigurationProperties;
	
	private AuthenticationService authenticationService;

	@BeforeEach
	void setUp() {
		this.jwtUtility = mock(JwtUtility.class);
		this.cacheManager = mock(CacheManager.class);
		this.userRepository = mock(UserRepository.class);
		this.passwordEncoder = mock(PasswordEncoder.class);
		this.refreshTokenGenerator = mock(RefreshTokenGenerator.class);
		this.tokenConfigurationProperties = mock(TokenConfigurationProperties.class);
		this.authenticationService = new AuthenticationService(jwtUtility, cacheManager, userRepository, passwordEncoder, refreshTokenGenerator, tokenConfigurationProperties);
	}

	@Test
	void successfullLogin() {
		// Prepare
		final String encryptedPassword = "blablabla";
		final String rawPassword = "secret";
		final String emailId = "hehe@hoho.com";
		final UUID userId = UUID.randomUUID();
		var user = mock(User.class);
		var userLoginRequestDto = mock(UserLoginRequestDto.class);
		when(user.getId()).thenReturn(userId);
		when(user.getPassword()).thenReturn(encryptedPassword);
		when(userLoginRequestDto.getPassword()).thenReturn(rawPassword);
		when(userLoginRequestDto.getEmailId()).thenReturn(emailId);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(rawPassword, encryptedPassword)).thenReturn(true);
		
		final var refreshTokenProperties = mock(RefreshToken.class);
		when(tokenConfigurationProperties.getRefreshToken()).thenReturn(refreshTokenProperties);
		when(refreshTokenProperties.getValidity()).thenReturn(20);

		final String accessToken = "test-refresh-token";
		final String refreshToken = "test-refresh-token";
		when(jwtUtility.generateAccessToken(user)).thenReturn(accessToken);
		when(refreshTokenGenerator.generate()).thenReturn(refreshToken);

		// Call
		final var response = authenticationService.login(userLoginRequestDto);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getAccessToken()).isEqualTo(accessToken);
		assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
		verify(userRepository, times(1)).findByEmailId(emailId);
		verify(jwtUtility, times(1)).generateAccessToken(user);
		verify(refreshTokenGenerator, times(1)).generate();
		verify(passwordEncoder, times(1)).matches(rawPassword, encryptedPassword);
	}

	@Test
	void loginUsingInvalidEmailId() {
		// Prepare
		final String emailId = "hehe@hoho.com";
		final String errorMessage = "Invalid login credentials provided";
		var userLoginRequestDto = mock(UserLoginRequestDto.class);
		when(userLoginRequestDto.getEmailId()).thenReturn(emailId);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.empty());

		// Call and Verify
		final var errorResponse = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationService.login(userLoginRequestDto));
		assertThat(errorResponse.getMessage()).contains(errorMessage);
		assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(userRepository, times(1)).findByEmailId(emailId);
	}

	@Test
	void loginUsingWrongPassword() {
		// Prepare
		final String encryptedPassword = "blablabla";
		final String rawPassword = "secret";
		final String emailId = "hehe@hoho.com";
		final String errorMessage = "Invalid login credentials provided";
		var user = mock(User.class);
		var userLoginRequestDto = mock(UserLoginRequestDto.class);
		when(user.getPassword()).thenReturn(encryptedPassword);
		when(userLoginRequestDto.getPassword()).thenReturn(rawPassword);
		when(userLoginRequestDto.getEmailId()).thenReturn(emailId);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(rawPassword, encryptedPassword)).thenReturn(false);

		// Call and Verify
		final var errorResponse = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationService.login(userLoginRequestDto));
		assertThat(errorResponse.getMessage()).contains(errorMessage);
		assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(userRepository, times(1)).findByEmailId(emailId);
		verify(passwordEncoder, times(1)).matches(rawPassword, encryptedPassword);
	}

	@Test
	void refreshTokenWithExpiredRefreshToken() {
		// Prepare
		final String refreshToken = "Refresh token JWT";
		when(cacheManager.fetch(eq(refreshToken), eq(UUID.class))).thenReturn(Optional.empty());

		// Call and Verify
		final var errorResponse = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationService.refreshToken(refreshToken));
		assertThat(errorResponse.getReason()).isEqualTo("Authentication failure: Token missing, invalid, revoked or expired");
		assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(cacheManager, times(1)).fetch(eq(refreshToken), eq(UUID.class));
	}

	@Test
	void tokenRefreshalSuccess() {
		// Prepare
		final String refreshToken = "Refresh token JWT";
		final UUID userId = UUID.randomUUID();
		final var user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		when(cacheManager.fetch(eq(refreshToken), eq(UUID.class))).thenReturn(Optional.of(userId));
		when(userRepository.getReferenceById(userId)).thenReturn(user);
		final String accessToken = "Access token JWT";
		when(jwtUtility.generateAccessToken(user)).thenReturn(accessToken);

		// Call
		final var response = authenticationService.refreshToken(refreshToken);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getAccessToken()).isEqualTo(accessToken);
		verify(jwtUtility, times(1)).generateAccessToken(user);
	}

}
