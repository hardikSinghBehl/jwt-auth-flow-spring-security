package com.behl.cerberus.security.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ApiPathExclusion {

	@Getter
	@RequiredArgsConstructor
	public enum GetApiPathExclusion {
	
		SWAGGER_UI("/swagger-ui**/**"), SWAGGER_API_V3_DOCS("/v3/api-docs**/**");
		
		private final String path;

	}

	@Getter
	@RequiredArgsConstructor
	public enum PostApiPathExclusion {

		SIGN_UP("/users"), LOGIN("/auth/login");

		private final String path;
	}

	@Getter
	@RequiredArgsConstructor
	public enum PutApiPathExclusion {

		REFRESH_TOKEN("/auth/refresh");

		private final String path;
	}

}
