package com.behl.cerberus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.behl.cerberus.utility.CacheManager;
import com.behl.cerberus.utility.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;

class TokenRevocationServiceTest {

	private final JwtUtility jwtUtility = mock(JwtUtility.class);
	private final CacheManager cacheManager = mock(CacheManager.class);
	private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
	private final TokenRevocationService tokenRevocationService = new TokenRevocationService(jwtUtility, cacheManager, httpServletRequest);

	private static final String AUTHORIZATION_HEADER = "Authorization";

	@Test
	void shouldAddTokenJtiToCacheForRevocation() {
		// set up access token in current HTTP request header
		final var accessToken = "test-access-token";
		when(httpServletRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn(accessToken);
		
		// set up details extraction from access token
		final var jti = UUID.randomUUID().toString();
		final var timeUntilExpiration = mock(Duration.class);
		when(jwtUtility.getJti(accessToken)).thenReturn(jti);
		when(jwtUtility.getTimeUntilExpiration(accessToken)).thenReturn(timeUntilExpiration);
		
		// set JTI and TTL to be saved in cache
		doNothing().when(cacheManager).save(jti, timeUntilExpiration);

		// invoke method under test
		tokenRevocationService.revoke();

		// verify mock interactions
		verify(httpServletRequest).getHeader(AUTHORIZATION_HEADER);
		verify(jwtUtility).getJti(accessToken);
		verify(jwtUtility).getTimeUntilExpiration(accessToken);
		verify(cacheManager).save(jti, timeUntilExpiration);
	}
	
	@Test
	void shouldThrowExceptionForNonExistentAuthorizationHeader() {
		// set up no authorization header in current HTTP request
		when(httpServletRequest.getHeader(AUTHORIZATION_HEADER)).thenReturn(null);

		// invoke method under test and verify exception and mock interaction
		assertThrows(IllegalStateException.class, tokenRevocationService::revoke);
		verify(httpServletRequest).getHeader(AUTHORIZATION_HEADER);
	}

	@Test
	void shouldReturnTrueIfAccessTokenIsRevoked() {
		// set up JTI corresponding to access token to be present in cache 
		final var accessToken = "test-access-token";
		final var jti = UUID.randomUUID().toString();
		when(jwtUtility.getJti(accessToken)).thenReturn(jti);
		when(cacheManager.isPresent(jti)).thenReturn(Boolean.TRUE);

		// invoke method under test
		final var response = tokenRevocationService.isRevoked(accessToken);

		// verify response and mock interactions
		assertThat(response).isTrue();
		verify(jwtUtility).getJti(accessToken);
		verify(cacheManager).isPresent(jti);
	}

	@Test
	void shouldReturnFalseIfAccessTokenJtiNotPresentInCache() {
		// set up JTI corresponding to access token to be absent in cache 
		final var accessToken = "test-access-token";
		final var jti = UUID.randomUUID().toString();
		when(jwtUtility.getJti(accessToken)).thenReturn(jti);
		when(cacheManager.isPresent(jti)).thenReturn(Boolean.FALSE);

		// invoke method under test
		final var response = tokenRevocationService.isRevoked(accessToken);

		// verify response and mock interactions
		assertThat(response).isFalse();
		verify(jwtUtility).getJti(accessToken);
		verify(cacheManager).isPresent(jti);
	}

}