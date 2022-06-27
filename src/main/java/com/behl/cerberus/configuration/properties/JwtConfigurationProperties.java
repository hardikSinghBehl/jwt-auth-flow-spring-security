package com.behl.cerberus.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "com.behl.cerberus")
@Data
public class JwtConfigurationProperties {

	private JWT jwt = new JWT();

	@Data
	public class JWT {
		private String secretKey;
		private AccessToken accessToken = new AccessToken();
		private RefreshToken refreshToken = new RefreshToken();

		@Data
		public class AccessToken {
			/**
			 * validity of access-token in minutes
			 */
			private Integer validity;
		}

		@Data
		public class RefreshToken {
			/**
			 * validity of refresh-token in days
			 */
			private Integer validity;
		}
	}

}