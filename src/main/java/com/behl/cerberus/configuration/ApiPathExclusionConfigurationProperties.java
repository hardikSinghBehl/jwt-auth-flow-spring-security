package com.behl.cerberus.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties controlling the API paths that bypass system imposed
 * authentication and authorization checks. These are referenced when
 * configuring <code>org.springframework.security.web.SecurityFilterChain</code>
 * as well as in the Authentication filter which populates the security context
 * corresponding to current HttpRequest.
 * 
 * @see com.behl.cerberus.configuration.SecurityConfiguration
 * @see com.behl.cerberus.filter.JwtAuthenticationFilter
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "com.behl.cerberus.unsecured.api-path")
public class ApiPathExclusionConfigurationProperties {
	
	/**
	 * Determines whether Swagger v3 API documentation and related endpoints are
	 * accessible bypassing Authentication and Authorization checks. Swagger
	 * endpoints are restricted by default.
	 * 
	 * Can be used in profile-specific configuration files to control
	 * access based on current environments.
	 */
	private boolean swaggerV3;
	
	/**
	 * List of HTTP GET API endpoints that are exempted from Authentication and
	 * Authorization checks. Wildcards can be used to specify API paths.
	 */
	private List<String> get;
	
	/**
	 * List of HTTP POST API endpoints that are exempted from Authentication and
	 * Authorization checks. Wildcards can be used to specify API paths.
	 */
	private List<String> post;
	
	/**
	 * List of HTTP PUT API endpoints that are exempted from Authentication and
	 * Authorization checks. Wildcards can be used to specify API paths.
	 */
	private List<String> put;

}
