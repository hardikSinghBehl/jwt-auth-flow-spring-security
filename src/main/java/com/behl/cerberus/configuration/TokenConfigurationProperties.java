package com.behl.cerberus.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties controlling token generation, validation, and
 * expiration within the application.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "com.behl.cerberus.token")
public class TokenConfigurationProperties {
		
	@Valid
	private AccessToken accessToken = new AccessToken();
	
	@Valid
	private RefreshToken refreshToken = new RefreshToken();

	@Getter
	@Setter
	public class AccessToken {
		
		/**
		 * The symmetric secret-key used for both signing and verifying the signature of
		 * received access token(s) to ensure authenticity.
		 * 
		 * @see com.behl.cerberus.utility.JwtUtility
		 */
		@NotBlank
		private String secretKey;

		/**
		 * The validity period of JWT access token(s) in minutes, post which the token
		 * expires and can no longer be used for authentication.
		 * 
		 * @see com.behl.cerberus.utility.JwtUtility
		 */
		@NotNull
		@Positive
		private Integer validity;
		
	}

	@Getter
	@Setter
	public class RefreshToken {
		
		/**
		 * The validity period (in minutes) for generated refresh token(s). After this
		 * period, the refresh token will expire and the user will need to
		 * re-authenticate with the system.
		 * 
		 * The tokens are stored in cache, and the validity duration is referenced as
		 * it's TTL (Time to Live).
		 * 
		 * @see com.behl.cerberus.service.AuthenticationService
		 */
		@NotNull
		@Positive
		private Integer validity;
		
	}

}