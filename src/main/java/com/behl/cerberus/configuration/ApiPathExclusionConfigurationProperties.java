package com.behl.cerberus.configuration;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.behl.cerberus.unsecured.api-path")
public class ApiPathExclusionConfigurationProperties {
	
	/**
	 * Determines whether Swagger v3 API documentation and related endpoints are
	 * accessible bypassing Authentication and Authorization checks. Swagger
	 * endpoints are restricted by default.
	 */
	private boolean swaggerV3;
	
	/**
	 * List of HTTP GET API endpoints that are exempted from Authentication and
	 * Authorization checks.
	 */
	private List<String> get;
	
	/**
	 * List of HTTP POST API endpoints that are exempted from Authentication and
	 * Authorization checks.
	 */
	private List<String> post;
	
	/**
	 * List of HTTP PUT API endpoints that are exempted from Authentication and
	 * Authorization checks.
	 */
	private List<String> put;

}
