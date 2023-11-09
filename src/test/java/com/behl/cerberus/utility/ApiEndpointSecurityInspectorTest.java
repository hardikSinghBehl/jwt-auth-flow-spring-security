package com.behl.cerberus.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.behl.cerberus.configuration.ApiPathExclusionConfigurationProperties;

import jakarta.servlet.http.HttpServletRequest;

class ApiEndpointSecurityInspectorTest {
	
	private final ApiPathExclusionConfigurationProperties apiPathExclusionConfigurationProperties = mock(ApiPathExclusionConfigurationProperties.class);
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector = new ApiEndpointSecurityInspector(apiPathExclusionConfigurationProperties);

	@Test
	void shouldReturnTrueIfHttpRequestDirectedTowardsUnsecuredApiEndpoint() {
		// defining API paths
		final var destinationApiPath = "/api/v1/unit-tests-reports";
		final var configuredUnsecuredPath = "/api/v1/unit-tests**";
		
		// simulating incoming HTTP request
		final var httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getMethod()).thenReturn(HttpMethod.POST.name());
		when(httpRequest.getRequestURI()).thenReturn(destinationApiPath);
		
		// configuring public/unsecured api path
		when(apiPathExclusionConfigurationProperties.getPost()).thenReturn(List.of(configuredUnsecuredPath));
		
		// invoke method under test
		final var result = apiEndpointSecurityInspector.isUnsecureRequest(httpRequest);
		
		// assert response and verify mock interactions
		assertThat(result).isTrue();
		verify(httpRequest).getMethod();
		verify(httpRequest).getRequestURI();
		verify(apiPathExclusionConfigurationProperties).getPost();
	}
	
	@Test
	void shouldReturnFalseIfHttpRequestDirectedTowardsSecuredApiEndpoint() {
		// defining API paths
		final var destinationApiPath = "/api/v1/unit-tests-reports";
		final var configuredUnsecuredPath = "/api/v1/unit-tests-are-boring";
		
		// simulating incoming HTTP request
		final var httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getMethod()).thenReturn(HttpMethod.POST.name());
		when(httpRequest.getRequestURI()).thenReturn(destinationApiPath);
		
		// configuring public/unsecured api path
		when(apiPathExclusionConfigurationProperties.getPost()).thenReturn(List.of(configuredUnsecuredPath));
				
		// invoke method under test
		final var result = apiEndpointSecurityInspector.isUnsecureRequest(httpRequest);
		
		// assert response and verify mock interactions
		assertThat(result).isFalse();
		verify(httpRequest).getMethod();
		verify(httpRequest).getRequestURI();
		verify(apiPathExclusionConfigurationProperties).getPost();
	}
	
}