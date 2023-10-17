package com.behl.cerberus.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.behl.cerberus.utility.CacheManager;
import com.behl.cerberus.utility.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Service class handling token revocation in the application. It makes use of
 * JTI (JWT Token Identifier) and stores this as a key in the provisioned cache
 * if the current Access Token is to be revoked. All subsequent incoming HTTP
 * requests to secured API endpoint(s) are verified by evaluating the presence
 * of the received JTI in the cache.
 * 
 * @see com.behl.cerberus.filter.JwtAuthenticationFilter
 * @see com.behl.cerberus.exception.TokenVerificationException
 */
@Service
@RequiredArgsConstructor
public class TokenRevocationService {

	private final JwtUtility jwtUtility;
	private final CacheManager cacheManager;
	private final HttpServletRequest httpServletRequest;

	/**
	 * Revokes the current Access Token by storing its unique JWT Token Identifier
	 * (JTI) in provisioned cache. The key is stored with the TTL is calculated
	 * based on the token's expiration time. This prevents further use of the token
	 * for authentication.
	 * 
	 * @throws IllegalStateException if the "Authorization" header is not present in
	 *                               the request.
	 */
	public void revoke() {
		final var authHeader = Optional.ofNullable(httpServletRequest.getHeader("Authorization")).orElseThrow(IllegalStateException::new);
		final var jti = jwtUtility.getJti(authHeader);
		final var ttl = jwtUtility.getTimeUntilExpiration(authHeader);
		cacheManager.save(jti, ttl);
	}

	/**
	 * Checks if the provided JWT token has been revoked by the syetem. It verifies
	 * the presence of the JTI in the cache to determine if the token is revoked.
	 * 
	 * @param authHeader The Authorization header containing the JWT token.
	 * @return {@code true} if the token is revoked; {@code false} if not
	 */
	public boolean isRevoked(@NonNull final String authHeader) {
		final var jti = jwtUtility.getJti(authHeader);
		return cacheManager.isPresent(jti);
	}

}
