package com.behl.cerberus.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.behl.cerberus")
public class JwtConfigurationProperties {

	private JWT jwt = new JWT();

	@Getter
	@Setter
	public class JWT {
		
		private String secretKey;
		private AccessToken accessToken = new AccessToken();
		private RefreshToken refreshToken = new RefreshToken();

		@Getter
		@Setter
		public class AccessToken {
			
			/**
			 * validity of access-token in minutes
			 */
			private Integer validity;
			
		}

		@Getter
		@Setter
		public class RefreshToken {
			
			/**
			 * validity of refresh-token in days
			 */
			private Integer validity;
			
		}
		
	}

}