package com.behl.cerberus.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.behl.cerberus.filter.JwtAuthenticationFilter;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * Configuration class responsible for defining and configuring the security
 * settings for the application. It sets up the following components and
 * features:
 * <ul>
 *   <li>Configuration of non-secured public API endpoints.</li>
 *   <li>Configuration of authentication entry point to handle authentication
 *       failures during the request evaluation through the filter chain.</li>
 *   <li>Integration of a custom JWT filter into the security filter chain to ensure
 *       that all requests to private endpoints pass through the filter for
 *       authentication verification.</li>
 * </ul>
 *
 * @see com.behl.cerberus.filter.JwtAuthenticationFilter
 * @see com.behl.cerberus.configuration.CustomAuthenticationEntryPoint
 * @see com.behl.cerberus.utility.ApiEndpointSecurityInspector
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final ApiEndpointSecurityInspector apiEndpointSecurityInspector;
	
	@Bean
	@SneakyThrows
	public SecurityFilterChain configure(final HttpSecurity http)  {
		http
			.cors(corsConfigurer -> corsConfigurer.disable())
			.csrf(csrfConfigurer -> csrfConfigurer.disable())
			.exceptionHandling(exceptionConfigurer -> exceptionConfigurer.authenticationEntryPoint(customAuthenticationEntryPoint))
			.sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(authManager -> {
					authManager
						.requestMatchers(HttpMethod.GET, apiEndpointSecurityInspector.getPublicGetEndpoints().toArray(String[]::new)).permitAll()
						.requestMatchers(HttpMethod.POST, apiEndpointSecurityInspector.getPublicPostEndpoints().toArray(String[]::new)).permitAll()
						.requestMatchers(HttpMethod.PUT, apiEndpointSecurityInspector.getPublicPutEndpoints().toArray(String[]::new)).permitAll()
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