package com.behl.cerberus.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.behl.cerberus.security.constant.GetApiPathExclusion;
import com.behl.cerberus.security.constant.OpenApiPathExclusion;
import com.behl.cerberus.security.filter.JwtAuthenticationFilter;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {

		http.cors().and().csrf().disable().exceptionHandling().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers(List.of(OpenApiPathExclusion.values()).stream().map(apiPath -> apiPath.getPath())
						.toArray(String[]::new))
				.permitAll()
				.antMatchers(HttpMethod.GET,
						List.of(GetApiPathExclusion.values()).stream().map(apiPath -> apiPath.getPath())
								.toArray(String[]::new))
				.permitAll().anyRequest().authenticated().and()
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

}