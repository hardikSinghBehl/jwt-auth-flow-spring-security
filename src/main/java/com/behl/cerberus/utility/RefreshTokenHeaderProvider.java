package com.behl.cerberus.utility;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Utility class capable of extracting <code>Refresh Token</code> value from the
 * current HTTP request's headers for subsequent use in the application.
 * 
 * @see com.behl.cerberus.configuration.OpenApiConfiguration
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenHeaderProvider {
	
	private final HttpServletRequest httpServletRequest;
	
	public static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";
	
	/**
	 * Retrieves <code>Refresh Token</code> value from the current HTTP request's
	 * header
	 * 
	 * @return An optional containing the refresh token value if sent as part of
	 *         current HTTP request headers, or an empty optional if the token is
	 *         not present or contains blank value.
	 */
	public Optional<String> getRefreshToken() {
		return Optional.ofNullable(httpServletRequest.getHeader(REFRESH_TOKEN_HEADER))
				.filter(value -> StringUtils.isNotBlank(value));
	}

}
