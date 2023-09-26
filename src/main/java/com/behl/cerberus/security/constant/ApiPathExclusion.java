package com.behl.cerberus.security.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiPathExclusion {

	@Getter
	@AllArgsConstructor
	public enum GetApiPathExclusion {

		SWAGGER_API_V2_DOCS("/v2/api-docs"),
		SWAGGER_RESOURCE_CONFIGURATION("/swagger-resources/configuration/ui"), SWAGGER_RESOURCES("/swagger-resources"),
		SWAGGER_RESOURCES_SECURITY_CONFIGURATION("/swagger-resources/configuration/security"),
		SWAGGER_UI_HTML("swagger-ui.html"), WEBJARS("/webjars/**"), SWAGGER_UI("/swagger-ui/**"),
		SWAGGER_API_V3_DOCS("/v3/api-docs/**"), SWAGGER_CONFIGURATION("/configuration/**"), SWAGGER("/swagger*/**");

		private final String path;

	}

	@Getter
	@AllArgsConstructor
	public enum PostApiPathExclusion {

		SIGN_UP("/users"), LOGIN("/auth/login");

		private final String path;
	}

	@Getter
	@AllArgsConstructor
	public enum PutApiPathExclusion {

		REFRESH_TOKEN("/auth/refresh");

		private final String path;
	}

}
