package com.behl.cerberus.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.behl.cerberus.configuration.TokenConfigurationProperties;
import com.behl.cerberus.configuration.TokenConfigurationProperties.AccessToken;
import com.behl.cerberus.entity.User;
import com.behl.cerberus.entity.UserStatus;

import io.jsonwebtoken.io.Encoders;
import net.bytebuddy.utility.RandomString;

class JwtUtilityTest {
	
	private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
	private static final String JWT_STRUCTURE_REGEX = "^[^.]+\\.[^.]+\\.[^.]+$";
	
	private final TokenConfigurationProperties tokenConfigurationProperties = mock(TokenConfigurationProperties.class);
    private final static String issuer = "unit-test-issuer";
	private final JwtUtility jwtUtility = new JwtUtility(tokenConfigurationProperties, issuer);
			
	@Test
	void shouldGenerateValidAccessTokenForUserEntityWithRequiredClaims() {
		// Prepare test user entity
		final var userId = UUID.randomUUID();
		final var userStatus = UserStatus.PENDING_APPROVAL;
		final var user = mock(User.class);
		when(user.getId()).thenReturn(userId);
		when(user.getUserStatus()).thenReturn(userStatus);
		
		// configure token configuration
		final var accessTokenValidity = 1;
		final var secretKey = Encoders.BASE64.encode(RandomString.make(32).getBytes());
		final var accessTokenConfiguration = mock(AccessToken.class);
		when(tokenConfigurationProperties.getAccessToken()).thenReturn(accessTokenConfiguration);
		when(accessTokenConfiguration.getValidity()).thenReturn(accessTokenValidity);
		when(accessTokenConfiguration.getSecretKey()).thenReturn(secretKey);
		
		// Generate access token for user entity
		final var accessToken = jwtUtility.generateAccessToken(user);
		
		// Validate the generated access token and verify mock interactions
		assertThat(accessToken).isNotBlank().matches(JWT_STRUCTURE_REGEX);
		verify(accessTokenConfiguration).getSecretKey();
		verify(accessTokenConfiguration).getValidity();
		verify(user).getId();
		verify(user).getUserStatus();
		
		// Extract claims from generated access token
		final var extractedUserId = jwtUtility.extractUserId(accessToken);
		final var extractedJti = jwtUtility.getJti(accessToken);
		final var extractedAuthorities = jwtUtility.getAuthority(accessToken);
	    final var timeUntilExpiration = jwtUtility.getTimeUntilExpiration(accessToken);

	    // Assert validity of extracted user ID
		assertThat(extractedUserId)
			.isNotNull()
			.isInstanceOf(UUID.class)
			.isEqualTo(userId);
		
		// Assert validity of JTI (JWT Token Identifier)
		assertThat(extractedJti)
			.isNotNull()
			.isNotBlank()
			.matches(UUID_REGEX);
				
		// Assert that the extracted authorities match user status scopes
		assertThat(extractedAuthorities)
			.isNotEmpty()
			.doesNotContainNull()
			.containsExactlyElementsOf(userStatus.getScopes().stream().map(SimpleGrantedAuthority::new).toList());
		
		// Assert that the time until token expiration is within the configured validity
	    assertThat(timeUntilExpiration)
	    	.isNotNull()
	    	.isLessThan(Duration.ofMinutes(accessTokenValidity));
	}
	
	@Test
	void shouldThrowIllegalArgumentExceptionForNullArguments() {
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.getJti(null));
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.getAuthority(null));
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.extractUserId(null));
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.generateAccessToken(null));
		assertThrows(IllegalArgumentException.class, () -> jwtUtility.getTimeUntilExpiration(null));
	}
	
}
