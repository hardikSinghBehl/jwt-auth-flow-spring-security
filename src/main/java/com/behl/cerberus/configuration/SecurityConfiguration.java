package com.behl.cerberus.configuration;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.behl.cerberus.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApiPathExclusionConfigurationProperties.class)
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ApiPathExclusionConfigurationProperties apiPathExclusionConfigurationProperties;
	private static final List<String> SWAGGER_V3_PATHS = List.of("/swagger-ui**/**", "/v3/api-docs**/**");

	@Bean
	@SneakyThrows
	public SecurityFilterChain configure(final HttpSecurity http)  {
		final var unsecuredGetEndpoints = apiPathExclusionConfigurationProperties.getGet();
		final var unsecuredPostEndpoints = apiPathExclusionConfigurationProperties.getPost();
		final var unsecuredPutEndpoints = apiPathExclusionConfigurationProperties.getPut();
		
		if (Boolean.TRUE.equals(apiPathExclusionConfigurationProperties.isSwaggerV3())) {
			unsecuredGetEndpoints.addAll(SWAGGER_V3_PATHS);
		}
		
		http
			.cors(corsConfigurer -> corsConfigurer.disable())
			.csrf(csrfConfigurer -> csrfConfigurer.disable())
			.sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authManager -> {
					authManager
						.requestMatchers(HttpMethod.GET, unsecuredGetEndpoints.toArray(String[]::new)).permitAll()
						.requestMatchers(HttpMethod.POST, unsecuredPostEndpoints.toArray(String[]::new)).permitAll()
						.requestMatchers(HttpMethod.PUT, unsecuredPutEndpoints.toArray(String[]::new)).permitAll()
					.anyRequest().authenticated();
				})
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}