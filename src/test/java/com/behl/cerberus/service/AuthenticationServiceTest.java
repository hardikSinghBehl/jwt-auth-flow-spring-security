package com.behl.cerberus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.behl.cerberus.dto.RefreshTokenRequestDto;
import com.behl.cerberus.dto.TokenSuccessResponseDto;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.repository.UserRepository;
import com.behl.cerberus.security.utility.JwtUtility;

class AuthenticationServiceTest {

	private AuthenticationService authenticationService;
	private UserRepository userRepository;
	private PasswordEncoder passwordEncoder;
	private JwtUtility jwtUtility;

	@BeforeEach
	void setUp() {
		this.userRepository = mock(UserRepository.class);
		this.passwordEncoder = mock(PasswordEncoder.class);
		this.jwtUtility = mock(JwtUtility.class);
		this.authenticationService = new AuthenticationService(userRepository, passwordEncoder, jwtUtility);
	}

	@Test
	void successfullLogin() {
		// Prepare
		final String encryptedPassword = "blablabla";
		final String rawPassword = "secret";
		final String emailId = "hehe@hoho.com";
		var user = mock(User.class);
		var userLoginRequestDto = mock(UserLoginRequestDto.class);
		when(user.getPassword()).thenReturn(encryptedPassword);
		when(userLoginRequestDto.getPassword()).thenReturn(rawPassword);
		when(userLoginRequestDto.getEmailId()).thenReturn(emailId);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(rawPassword, encryptedPassword)).thenReturn(true);

		final String accessToken = "Access token JWT";
		final String refreshToken = "Refresh token JWT";
		final LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(30);
		when(jwtUtility.generateAccessToken(user)).thenReturn(accessToken);
		when(jwtUtility.generateRefreshToken(user)).thenReturn(refreshToken);
		when(jwtUtility.extractExpirationTimestamp(accessToken)).thenReturn(expirationTime);

		// Call
		final var response = authenticationService.login(userLoginRequestDto);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getAccessToken()).isEqualTo(accessToken);
		assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
		assertThat(response.getExpiresAt()).isEqualTo(expirationTime);
		verify(userRepository, times(1)).findByEmailId(emailId);
		verify(jwtUtility, times(1)).generateAccessToken(user);
		verify(jwtUtility, times(1)).generateRefreshToken(user);
		verify(jwtUtility, times(1)).extractExpirationTimestamp(accessToken);
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
		var refreshTokenRequestDto = mock(RefreshTokenRequestDto.class);
		when(refreshTokenRequestDto.getRefreshToken()).thenReturn(refreshToken);
		when(jwtUtility.isTokenExpired(refreshToken)).thenReturn(true);

		// Call and Verify
		final var errorResponse = Assertions.assertThrows(ResponseStatusException.class,
				() -> authenticationService.refreshToken(refreshTokenRequestDto));
		assertThat(errorResponse.getMessage()).contains("Token expired");
		assertThat(errorResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
		verify(jwtUtility, times(1)).isTokenExpired(refreshToken);
	}

	@Test
	void tokenRefreshalSuccess() {
		// Prepare
		final String refreshToken = "Refresh token JWT";
		final UUID userId = UUID.fromString("304227a6-5938-4bbc-9c3c-a13520372abc");
		var user = mock(User.class);
		var refreshTokenRequestDto = mock(RefreshTokenRequestDto.class);
		when(refreshTokenRequestDto.getRefreshToken()).thenReturn(refreshToken);
		when(jwtUtility.isTokenExpired(refreshToken)).thenReturn(false);
		when(jwtUtility.extractUserId(refreshToken)).thenReturn(userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		final String accessToken = "Access token JWT";
		final LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(30);
		when(jwtUtility.generateAccessToken(user)).thenReturn(accessToken);
		when(jwtUtility.extractExpirationTimestamp(accessToken)).thenReturn(expirationTime);

		// Call
		final var response = authenticationService.refreshToken(refreshTokenRequestDto);

		// Verify
		assertThat(response).isNotNull();
		assertThat(response).isInstanceOf(TokenSuccessResponseDto.class);
		assertThat(response.getAccessToken()).isEqualTo(accessToken);
		assertThat(response.getExpiresAt()).isEqualTo(expirationTime);
		verify(jwtUtility, times(1)).generateAccessToken(user);
		verify(jwtUtility, times(0)).generateRefreshToken(user);
		verify(jwtUtility, times(1)).extractExpirationTimestamp(accessToken);
		verify(jwtUtility, times(1)).isTokenExpired(refreshToken);
		verify(jwtUtility, times(1)).extractUserId(refreshToken);
	}

}
