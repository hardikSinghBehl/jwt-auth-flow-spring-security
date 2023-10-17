package com.behl.cerberus.utility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.behl.cerberus.configuration.TokenConfigurationProperties;
import com.behl.cerberus.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Utility class for JWT (JSON Web Token) operations, responsible for handling
 * JWT generation, signature verification, and extracting required claims from
 * JWT tokens. It interacts with the application's token configuration
 * properties to ensure correct token creation and validation.
 * 
 * @see com.behl.cerberus.configuration.TokenConfigurationProperties
 * @see com.behl.cerberus.filter.JwtAuthenticationFilter
 * @see com.behl.cerberus.entity.UserStatus
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(TokenConfigurationProperties.class)
public class JwtUtility {

	private final TokenConfigurationProperties tokenConfigurationProperties;
	
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String SCOPE_CLAIM_NAME = "scp";

	@Value("${spring.application.name}")
	private String issuer;

	/**
	 * Extracts user's ID from a given JWT token signifying an authenticated
	 * user.
	 * 
	 * @param token The JWT token from which to extract the user's ID.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @return The authenticated user's unique identifier (ID) in UUID format.
	 */
	public UUID extractUserId(@NonNull final String token) {
		final var audience = extractClaim(token, Claims::getAudience);
		return UUID.fromString(audience);
	}
	
	/**
	 * Extracts given JWT token's unique identifier (JTI).
	 * 
	 * @param token The JWT token from which to extract the JTI.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @return JTI (JWT Token Identifier) assigned to the JWT token.
	 */
	public String getJti(@NonNull final String token) {
		return extractClaim(token, Claims::getId);
	}

	/**
	 * Generates an access token corresponding to provided user entity based on
	 * configured settings. The generated access token can be used to perform tasks
	 * on behalf of the user on subsequent HTTP calls to the application until it
	 * expires or is revoked.
	 * 
	 * @param user The user for whom to generate an access token.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @return The generated JWT access token.
	 */
	public String generateAccessToken(@NonNull final User user) {
		final var jti = String.valueOf(UUID.randomUUID());
		final var accessTokenValidity = tokenConfigurationProperties.getAccessToken().getValidity();
		final var expiration = TimeUnit.MINUTES.toMillis(accessTokenValidity);
		final var secretKey = tokenConfigurationProperties.getAccessToken().getSecretKey();
		final var currentTimestamp = new Date(System.currentTimeMillis());
		final var expirationTimestamp = new Date(System.currentTimeMillis() + expiration);
		final var scopes = user.getUserStatus().getScopes().stream().collect(Collectors.joining(StringUtils.SPACE));
		
		final var claims = new DefaultClaims();
		claims.put(SCOPE_CLAIM_NAME, scopes);
		
		return Jwts.builder()
				.setClaims(claims)
				.setId(jti)
				.setIssuer(issuer)
				.setIssuedAt(currentTimestamp)
				.setExpiration(expirationTimestamp)
				.setAudience(String.valueOf(user.getId()))
				.signWith(SignatureAlgorithm.HS256, secretKey).compact();
	}
	
	/**
	 * Extracts Granted Authorities from the scp claim of a JWT token. The scp claim
	 * contains space-separated permissions, which are transformed into a list of
	 * Granted Authorities representing user permissions or roles.
	 *
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @return A List of GrantedAuthority denoting user permissions.
	 */
	public List<GrantedAuthority> getAuthority(@NonNull final String token){
		final var scopes = extractClaim(token, claims -> claims.get(SCOPE_CLAIM_NAME, String.class));
		return Arrays.stream(scopes.split(StringUtils.SPACE))
					.map(SimpleGrantedAuthority::new)
					.collect(Collectors.toList());
	}
	
	/**
	 * Retrieves the expiration timestamp of a given JWT token, which indicates the
	 * time after which the token would no longer be eligible for authenticating a
	 * user with the system.
	 * 
	 * @param token The JWT token from which to extract the expiration timestamp.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>
	 * @return The expiration timestamp.
	 */
	public LocalDateTime getExpirationTimestamp(@NonNull final String token) {
		final var expiration = extractClaim(token, Claims::getExpiration);
		return expiration.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
	}
	
	/**
	 * Calculates the <code>java.time.Duration</code> until a JWT token's
	 * expiration.
	 * 
	 * @param token The JWT token for which to calculate the time until expiration.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>
	 * @return The duration until token expiration.
	 */
	public Duration getTimeUntilExpiration(@NonNull final String token) {
	    final var expirationTimestamp = extractClaim(token, Claims::getExpiration).toInstant();
	    final var currentTimestamp = new Date().toInstant();
	    return Duration.between(currentTimestamp, expirationTimestamp);
	}

	private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
		final var secretKey = tokenConfigurationProperties.getAccessToken().getSecretKey();
		final var santizedToken = token.replace(BEARER_PREFIX, StringUtils.EMPTY);
		final var claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(santizedToken).getBody();
		return claimsResolver.apply(claims);
	}

}