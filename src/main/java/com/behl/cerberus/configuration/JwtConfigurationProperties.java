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
		     * The validity period of access token(s) in minutes.
		     */
			private Integer validity;
			
		}

		@Getter
		@Setter
		public class RefreshToken {
			
		    /**
		     * The validity period of refresh token(s) in minutes.
		     */
			private Integer validity;
			
		}
		
	}

}