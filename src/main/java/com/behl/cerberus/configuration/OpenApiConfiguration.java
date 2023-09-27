package com.behl.cerberus.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(OpenApiConfigurationProperties.class)
public class OpenApiConfiguration {

	private final OpenApiConfigurationProperties openApiConfigurationProperties;
	
	private static final String SECURITY_COMPONENT_NAME = "Bearer Authentication";	
	private static final String SECURITY_SCHEME = "Bearer";

	@Bean
	public OpenAPI customOpenAPI() {
		final var properties = openApiConfigurationProperties.getOpenApi();
		final var info = new Info().title(properties.getTitle()).version(properties.getApiVersion())
				.description(properties.getDescription());

		return new OpenAPI()
			    .info(info)
			    .components(new Components()
			        .addSecuritySchemes(SECURITY_COMPONENT_NAME,
			            new SecurityScheme()
			                .type(SecurityScheme.Type.HTTP)
			                .scheme(SECURITY_SCHEME))
			    )
			    .addSecurityItem(new SecurityRequirement().addList(SECURITY_COMPONENT_NAME));
	}
	
}