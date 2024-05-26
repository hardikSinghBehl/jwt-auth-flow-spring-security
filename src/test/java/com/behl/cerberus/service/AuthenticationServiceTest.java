package com.behl.cerberus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordDecision;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.behl.cerberus.configuration.TokenConfigurationProperties;
import com.behl.cerberus.configuration.TokenConfigurationProperties.RefreshToken;
import com.behl.cerberus.dto.UserLoginRequestDto;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.exception.InvalidCredentialsException;
import com.behl.cerberus.exception.TokenVerificationException;
import com.behl.cerberus.repository.UserRepository;
import com.behl.cerberus.utility.CacheManager;
import com.behl.cerberus.utility.JwtUtility;
import com.behl.cerberus.utility.RefreshTokenGenerator;

class AuthenticationServiceTest {
	
	private final JwtUtility jwtUtility = mock(JwtUtility.class);
	private final CacheManager cacheManager = mock(CacheManager.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final RefreshTokenGenerator refreshTokenGenerator = mock(RefreshTokenGenerator.class);
	private final CompromisedPasswordChecker compromisedPasswordChecker = mock(CompromisedPasswordChecker.class);
	private final TokenConfigurationProperties tokenConfigurationProperties = mock(TokenConfigurationProperties.class);
	
	private final AuthenticationService authenticationService = new AuthenticationService(jwtUtility, cacheManager, userRepository, passwordEncoder, refreshTokenGenerator, compromisedPasswordChecker, tokenConfigurationProperties);

	@Test
	void loginShouldThrowExceptionForNonRegisteredEmailId() {
		// prepare login request
		final var emailId = "unregistered@domain.ut";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		
		// set datasource to return no response for unregistered email-id
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.empty());
		
		// assert InvalidLoginCredentialsException is thrown for unregistered email-id
		assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(userLoginRequest));
		
		// verify mock interactions
		verify(userRepository).findByEmailId(emailId);
	}
	
	@Test
	void loginShouldThrowExceptionForInvalidPassword() {
		// prepare login request
		final var emailId = "mail@domain.ut";
		final var password = "test-password";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(password);
		
		// prepare datasource response
		final var encodedPassword = "test-encoded-password";
		final var user = mock(User.class);
		when(user.getPassword()).thenReturn(encodedPassword);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));
		
		// set password validation to fail
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(Boolean.FALSE);
		
		// assert InvalidLoginCredentialsException is thrown for invalid password
		assertThrows(InvalidCredentialsException.class, () -> authenticationService.login(userLoginRequest));

		// verify mock interactions
		verify(userRepository).findByEmailId(emailId);
		verify(passwordEncoder).matches(password, encodedPassword);
	}
	
	@Test
	void loginShouldThrowExceptionForCompromisedPassword() {
		// prepare login request
		final var emailId = "mail@domain.ut";
		final var password = "compromised-password";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(password);
		
		// prepare datasource response
		final var encodedPassword = "test-encoded-password";
		final var user = mock(User.class);
		when(user.getPassword()).thenReturn(encodedPassword);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));
		
		// set password validation to pass
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(Boolean.TRUE);
		
		// set compromised password check failure
		final var compromisedPasswordDecision = mock(CompromisedPasswordDecision.class);
		when(compromisedPasswordDecision.isCompromised()).thenReturn(Boolean.TRUE);
		when(compromisedPasswordChecker.check(password)).thenReturn(compromisedPasswordDecision);
		
		// assert CompromisedPasswordException is thrown for invalid password
		assertThrows(CompromisedPasswordException.class, () -> authenticationService.login(userLoginRequest));

		// verify mock interactions
		verify(userRepository).findByEmailId(emailId);
		verify(passwordEncoder).matches(password, encodedPassword);
		verify(compromisedPasswordChecker).check(password);
	}
	
	@Test
	void shouldReturnTokenResponseForValidLoginCredentials() {
		// prepare login request
		final var emailId = "mail@domain.ut";
		final var password = "test-password";
		final var userLoginRequest = mock(UserLoginRequestDto.class);
		when(userLoginRequest.getEmailId()).thenReturn(emailId);
		when(userLoginRequest.getPassword()).thenReturn(password);
		
		// prepare datasource response
		final var userId = UUID.randomUUID();
		final var encodedPassword = "test-encoded-password";
		final var user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		when(user.getPassword()).thenReturn(encodedPassword);
		when(userRepository.findByEmailId(emailId)).thenReturn(Optional.of(user));
		
		// set password validation to pass
		when(passwordEncoder.matches(password, encodedPassword)).thenReturn(Boolean.TRUE);

		// set compromised password check to pass
		final var compromisedPasswordDecision = mock(CompromisedPasswordDecision.class);
		when(compromisedPasswordDecision.isCompromised()).thenReturn(Boolean.FALSE);
		when(compromisedPasswordChecker.check(password)).thenReturn(compromisedPasswordDecision);
		
		// set token generation
		final var accessToken = "test-access-token";
		final var refreshToken = "test-refresh-token";
		when(jwtUtility.generateAccessToken(user)).thenReturn(accessToken);
		when(refreshTokenGenerator.generate()).thenReturn(refreshToken);
		
		// handle refresh token storage in cache
		final var refreshTokenValidity = new Random().nextInt(1, 100);
		final var refreshTokenConfiguration = mock(RefreshToken.class);
		when(refreshTokenConfiguration.getValidity()).thenReturn(refreshTokenValidity);
		when(tokenConfigurationProperties.getRefreshToken()).thenReturn(refreshTokenConfiguration);
		doNothing().when(cacheManager).save(refreshToken, userId, Duration.ofMinutes(refreshTokenValidity));		
	
		// invoke method under test
		final var response = authenticationService.login(userLoginRequest);
		
		// verify correctness of response values
		assertThat(response).isNotNull();
		assertThat(response.getAccessToken()).isNotBlank().isEqualTo(accessToken);
		assertThat(response.getRefreshToken()).isNotBlank().isEqualTo(refreshToken);
		
		// verify mock interactions
		verify(userRepository).findByEmailId(emailId);
		verify(passwordEncoder).matches(password, encodedPassword);
		verify(compromisedPasswordChecker).check(password);
		verify(jwtUtility).generateAccessToken(user);
		verify(refreshTokenGenerator).generate();
		verify(cacheManager).save(refreshToken, userId, Duration.ofMinutes(refreshTokenValidity));
	}
	
	@Test
	void tokenRefreshShouldThrowExceptionForInvalidOrExpiredRefreshToken() {
		// set up cache to return no response for invalid refresh token
		final var refreshToken = "test-refresh-token";
		when(cacheManager.fetch(refreshToken, UUID.class)).thenReturn(Optional.empty());
		
		// assert TokenVerificationException is thrown for invalid refresh token
		assertThrows(TokenVerificationException.class, () -> authenticationService.refreshToken(refreshToken));
		
		// verify mock interactions
		verify(cacheManager).fetch(refreshToken, UUID.class);
	}
	
	@Test
	void shouldReturnNewAccessTokenForValidRefreshToken() {
		// set up cache to return stored userId corresponding to valid refresh token
		final var userId = UUID.randomUUID();
		final var refreshToken = "test-refresh-token";
		when(cacheManager.fetch(refreshToken, UUID.class)).thenReturn(Optional.of(userId));

		// set up datasource to return user entity corresponding to userId
		final var user = mock(User.class);
		when(userRepository.getReferenceById(userId)).thenReturn(user);
		
		// set token generation
		final var accessToken = "test-access-token";
		when(jwtUtility.generateAccessToken(user)).thenReturn(accessToken);
		
		// invoke method under test
		final var response = authenticationService.refreshToken(refreshToken);
		
		// verify existence of new access token in response
		assertThat(response).isNotNull();
		assertThat(response.getAccessToken()).isNotBlank().isEqualTo(accessToken);
		
		// verify mock interactions
		verify(cacheManager).fetch(refreshToken, UUID.class);
		verify(userRepository).getReferenceById(userId);
		verify(jwtUtility).generateAccessToken(user);
	}
	
	@Test
	void shouldThrowIllegalArgumentExceptionForNullArguments() {
		assertThrows(IllegalArgumentException.class, () -> authenticationService.login(null));
		assertThrows(IllegalArgumentException.class, () -> authenticationService.refreshToken(null));
	}
	
}