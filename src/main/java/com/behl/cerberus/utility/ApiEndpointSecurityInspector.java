package com.behl.cerberus.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import com.behl.cerberus.configuration.ApiPathExclusionConfigurationProperties;

import io.swagger.v3.oas.models.PathItem.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Utility class responsible for inspecting the security of API endpoints by
 * determining whether a given HTTP request is destined for a secured or
 * unsecured API endpoint. It works in conjunction with the api paths mapped in
 * {@link ApiPathExclusionConfigurationProperties}.
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ApiPathExclusionConfigurationProperties.class)
public class ApiEndpointSecurityInspector {

	private final ApiPathExclusionConfigurationProperties apiPathExclusionConfigurationProperties;

	/**
	 * Checks if the provided HTTP request is directed towards an unsecured API endpoint.
	 *
	 * @param request The HTTP request to inspect.
	 * @return {@code true} if the request is to an unsecured API endpoint, {@code false} otherwise.
	 */
	public boolean isUnsecureRequest(@NonNull final HttpServletRequest request) {
		final var requestHttpMethod = HttpMethod.valueOf(request.getMethod());
		var unsecuredApiPaths = getUnsecuredApiPaths(requestHttpMethod);
		unsecuredApiPaths = Optional.ofNullable(unsecuredApiPaths).orElseGet(ArrayList::new);

		return unsecuredApiPaths.stream().anyMatch(apiPath -> new AntPathMatcher().match(apiPath, request.getRequestURI()));
	}

	/**
	 * Retrieves the list of unsecured API paths based on the provided HTTP method. 
	 * The api endpoints paths are configured in the active {@code .yml} file and 
	 * mapped to the {@code ApiPathExclusionConfigurationProperties.java}
	 *
	 * @param httpMethod The HTTP method for which unsecured paths are to be retrieved.
	 * @return A list of unsecured API paths for the specified HTTP method.
	 */
	private List<String> getUnsecuredApiPaths(@NonNull final HttpMethod httpMethod) {
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