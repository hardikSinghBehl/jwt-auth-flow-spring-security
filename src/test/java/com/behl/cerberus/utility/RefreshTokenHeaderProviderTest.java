package com.behl.cerberus.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;

class RefreshTokenHeaderProviderTest {

	private final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
	private final RefreshTokenHeaderProvider refreshTokenHeaderProvider = new RefreshTokenHeaderProvider(httpServletRequest);

	private static final String REFRESH_TOKEN_HEADER = RefreshTokenHeaderProvider.REFRESH_TOKEN_HEADER;

	@Test
	void shouldGetRefreshTokenFromCurrentRequestHeader() {
		final var refreshToken = "test-refresh-token";
		when(httpServletRequest.getHeader(REFRESH_TOKEN_HEADER)).thenReturn(refreshToken);

		final var response = refreshTokenHeaderProvider.getRefreshToken();

		assertThat(response).isPresent().hasValue(refreshToken);
		verify(httpServletRequest).getHeader(REFRESH_TOKEN_HEADER);
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = { StringUtils.EMPTY })
	void shouldReturnEmptyOptionalForEmptyOrNonPresentHeader(@Nullable String headerValue) {
		// mock call to return null or an empty string as header value
		when(httpServletRequest.getHeader(REFRESH_TOKEN_HEADER)).thenReturn(headerValue);

		final var response = refreshTokenHeaderProvider.getRefreshToken();

		assertThat(response).isEmpty();
		verify(httpServletRequest).getHeader(REFRESH_TOKEN_HEADER);
	}

}