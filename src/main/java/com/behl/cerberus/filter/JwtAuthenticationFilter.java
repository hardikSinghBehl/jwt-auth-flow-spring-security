package com.behl.cerberus.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.behl.cerberus.configuration.ApiPathExclusionConfigurationProperties;
import com.behl.cerberus.utility.JwtUtility;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ApiPathExclusionConfigurationProperties.class)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtility jwtUtils;
	private final ApiPathExclusionConfigurationProperties apiPathExclusionConfigurationProperties;
	
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	@Override
	@SneakyThrows
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
		final var unsecuredApiBeingInvoked = isInvocationToUnsecuredApi(request);
		
		if (Boolean.FALSE.equals(unsecuredApiBeingInvoked)) {
			final var authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
	
			if (StringUtils.isNotEmpty(authorizationHeader)) {
				if (authorizationHeader.startsWith(BEARER_PREFIX)) {
					final var token = authorizationHeader.replace(BEARER_PREFIX, StringUtils.EMPTY);
					final var userId = jwtUtils.extractUserId(token);
					final var isTokenValid = jwtUtils.validateToken(token, userId);
					final var authorities = jwtUtils.getAuthority(token);
					
					if (Boolean.TRUE.equals(isTokenValid)) {
						final var authentication = new UsernamePasswordAuthenticationToken(userId, null, authorities);
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						SecurityContextHolder.getContext().setAuthentication(authentication);
					}
				}
			}
		}
		filterChain.doFilter(request, response);
	}
	
	private boolean isInvocationToUnsecuredApi(final HttpServletRequest request) {
	    final var requestHttpMethod = HttpMethod.valueOf(request.getMethod());
	    var unsecuredApiPaths = getUnsecuredApiPaths(requestHttpMethod);
	    unsecuredApiPaths = Optional.ofNullable(unsecuredApiPaths).orElseGet(ArrayList::new);

	    return unsecuredApiPaths.stream().anyMatch(apiPath -> new AntPathMatcher().match(apiPath, request.getRequestURI()));
	}
	
	private List<String> getUnsecuredApiPaths(final HttpMethod httpMethod) {
	    switch (httpMethod) {
	        case GET:
	            return apiPathExclusionConfigurationProperties.getGet();
	        case POST:
	            return apiPathExclusionConfigurationProperties.getPost();
	        case PUT:
	            return apiPathExclusionConfigurationProperties.getPut();
	        default:
	            return Collections.emptyList();
	    }
	}

}