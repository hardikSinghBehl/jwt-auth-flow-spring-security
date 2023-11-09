package com.behl.cerberus.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.behl.cerberus.exception.TokenVerificationException;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;
import com.behl.cerberus.utility.JwtUtility;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * JwtAuthenticationFilter is a custom filter registered with the spring
 * security filter chain and works in conjunction with the security
 * configuration, as defined in {@link com.behl.cerberus.configuration.SecurityConfiguration}. 
 * 
 * It is responsible for verifying the authenticity of incoming HTTP requests to
 * secured API endpoints by examining JWT token in the request header, verifying 
 * it's signature, expiration and evaluating it's presence in the token revocation list.
 * If authentication is successful, the filter populates the security context with
 * the user's unique identifier and the permissions associated with the
 * authenticated user which can be referenced by the application later.
 * 
 * This filter is only executed for secure endpoints, and is skipped if the incoming
 * request is destined to a non-secured public API endpoint.
 *
 * @see com.behl.cerberus.configuration.SecurityConfiguration
 * @see com.behl.cerberus.utility.ApiEndpointSecurityInspector
 * @see com.behl.cerberus.service.TokenRevocationService
 * @see com.behl.cerberus.utility.JwtUtility
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtility jwtUtils;
	private final TokenRevocationService tokenRevocationService; 
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;
	
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	@Override
	@SneakyThrows
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		final var unsecuredApiBeingInvoked = apiEndpointSecurityInspector.isUnsecureRequest(request);
		
		if (Boolean.FALSE.equals(unsecuredApiBeingInvoked)) {
			final var authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
	
			if (StringUtils.isNotEmpty(authorizationHeader)) {
				if (authorizationHeader.startsWith(BEARER_PREFIX)) {
					final var token = authorizationHeader.replace(BEARER_PREFIX, StringUtils.EMPTY);
					final var isTokenRevoked = tokenRevocationService.isRevoked(token);
					if (Boolean.TRUE.equals(isTokenRevoked)) {
						throw new TokenVerificationException();
					}
					
					final var userId = jwtUtils.extractUserId(token);
					final var authorities = jwtUtils.getAuthority(token);
					final var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		}
		filterChain.doFilter(request, response);
	}

}