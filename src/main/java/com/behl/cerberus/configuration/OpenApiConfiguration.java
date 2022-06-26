package com.behl.cerberus.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.behl.cerberus.configuration.properties.OpenApiConfigurationProperties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;

@Configuration
@EnableConfigurationProperties(OpenApiConfigurationProperties.class)
@AllArgsConstructor
public class OpenApiConfiguration {

	private final OpenApiConfigurationProperties openApiConfigurationProperties;

	@Bean
	public OpenAPI customOpenAPI() {
		final var properties = openApiConfigurationProperties.getOpenApi();
		final var security = properties.getSecurity();
		final var contact = properties.getContact();
		final var info = new Info().title(properties.getTitle()).version(properties.getApiVersion())
				.description(properties.getDescription())
				.contact(new Contact().email(contact.getEmail()).name(contact.getName()).url(contact.getUrl()));

		return new OpenAPI().info(info).addSecurityItem(new SecurityRequirement().addList(security.getName()))
				.components(new Components().addSecuritySchemes(security.getName(),
						new SecurityScheme().name(security.getName()).type(SecurityScheme.Type.HTTP)
								.scheme(security.getScheme()).bearerFormat(security.getBearerFormat())));
	}
}