package com.behl.cerberus.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.behl.cerberus")
public class OpenApiConfigurationProperties {

	private OpenAPI openApi = new OpenAPI();

	@Getter
	@Setter
	public class OpenAPI {
		
		private String title;
		private String description;
		private String apiVersion;
		private Contact contact = new Contact();
		private Security security = new Security();

		@Getter
		@Setter
		public class Contact {
			
			private String email;
			private String name;
			private String url;
			
		}

		@Getter
		@Setter
		public class Security {
			
			private String name;
			private String scheme;
			private String bearerFormat;
			
		}
		
	}

}