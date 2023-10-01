package com.behl.cerberus.utility;

import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenHeaderProvider {
	
	private final HttpServletRequest httpServletRequest;
	
	public Optional<String> getRefreshToken() {
		return Optional.ofNullable(httpServletRequest.getHeader("X-Refresh-Token"));
	}

}
