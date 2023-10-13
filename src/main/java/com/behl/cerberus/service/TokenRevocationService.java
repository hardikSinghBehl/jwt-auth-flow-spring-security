package com.behl.cerberus.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.behl.cerberus.utility.CacheManager;
import com.behl.cerberus.utility.JwtUtility;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TokenRevocationService {

	private final JwtUtility jwtUtility;
	private final CacheManager cacheManager;
	private final HttpServletRequest httpServletRequest;

	public void revoke() {
		final var authHeader = Optional.ofNullable(httpServletRequest.getHeader("Authorization")).orElseThrow(IllegalStateException::new);
		final var jti = jwtUtility.getJti(authHeader);
		final var ttl = jwtUtility.getTimeUntilExpiration(authHeader);
		cacheManager.save(jti, ttl);
	}

	public boolean isRevoked(@NonNull final String authHeader) {
		final var jti = jwtUtility.getJti(authHeader);
		return cacheManager.isPresent(jti);
	}

}
