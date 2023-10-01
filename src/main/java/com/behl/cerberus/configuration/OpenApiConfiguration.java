package com.behl.cerberus.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpenApiConfigurationProperties.class)
public class OpenApiConfiguration {

	private final OpenApiConfigurationProperties openApiConfigurationProperties;
	
	private static final String BEARER_AUTH_COMPONENT_NAME = "Bearer Authentication";	
	private static final String BEARER_AUTH_SCHEME = "Bearer";
	
	private static final String REFRESH_TOKEN_COMPONENT_NAME = "Refresh Token Header";
	private static final String REFRESH_TOKEN_HEADER = "X-Refresh-Token";

	@Bean
	public OpenAPI customOpenAPI() {
		final var properties = openApiConfigurationProperties.getOpenApi();
		final var info = new Info().title(properties.getTitle()).version(properties.getApiVersion())
				.description(properties.getDescription());

		return new OpenAPI()
			    .info(info)
			    .components(new Components()
			        .addSecuritySchemes(BEARER_AUTH_COMPONENT_NAME,
			            new SecurityScheme()
			                .type(SecurityScheme.Type.HTTP)
			                .scheme(BEARER_AUTH_SCHEME))
			        .addSecuritySchemes(REFRESH_TOKEN_COMPONENT_NAME,
			        	new SecurityScheme()
			        		.type(SecurityScheme.Type.APIKEY)
			        		.in(In.HEADER)
			        		.name(REFRESH_TOKEN_HEADER))
			    )
			    .addSecurityItem(new SecurityRequirement()
			    					.addList(BEARER_AUTH_COMPONENT_NAME)
			    					.addList(REFRESH_TOKEN_COMPONENT_NAME));
	}
	
}