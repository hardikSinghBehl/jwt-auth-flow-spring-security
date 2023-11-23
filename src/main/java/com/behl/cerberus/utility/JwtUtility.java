package com.behl.cerberus.utility;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

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
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.NonNull;

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
@EnableConfigurationProperties(TokenConfigurationProperties.class)
public class JwtUtility {
	
	private static final String BEARER_PREFIX = "Bearer ";
	private static final String SCOPE_CLAIM_NAME = "scp";

	private final TokenConfigurationProperties tokenConfigurationProperties;
	private final String issuer;
	
	public JwtUtility(final TokenConfigurationProperties tokenConfigurationProperties,
			@Value("${spring.application.name}") final String issuer) {
		this.tokenConfigurationProperties = tokenConfigurationProperties;
		this.issuer = issuer;
	}

	/**
	 * Extracts user's ID from a given JWT token signifying an authenticated
	 * user.
	 * 
	 * @param token The JWT token from which to extract the user's ID.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @throws IllegalStateException if provided token does not contain an audience claim
	 * @return The authenticated user's unique identifier (ID) in UUID format.
	 */
	public UUID extractUserId(@NonNull final String token) {
		final var audiences = extractClaim(token, Claims::getAudience);
		if (audiences != null && !audiences.isEmpty()) {
			final var audience = audiences.iterator().next();
			return UUID.fromString(audience);
		}
		throw new IllegalStateException("No audience claim found in given token");
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
		final var audience = String.valueOf(user.getId());
		final var accessTokenValidity = tokenConfigurationProperties.getAccessToken().getValidity();
		final var expiration = TimeUnit.MINUTES.toMillis(accessTokenValidity);
		final var currentTimestamp = new Date(System.currentTimeMillis());
		final var expirationTimestamp = new Date(System.currentTimeMillis() + expiration);
		final var scopes = user.getUserStatus().getScopes().stream().collect(Collectors.joining(StringUtils.SPACE));
		
		final var encodedSecretKey = tokenConfigurationProperties.getAccessToken().getSecretKey();
		final var secretKey = getSecretKey(encodedSecretKey);
		
		final var claims = new HashMap<String, String>();
		claims.put(SCOPE_CLAIM_NAME, scopes);
		
		return Jwts.builder()
				.claims(claims)
				.id(jti)
				.issuer(issuer)
				.issuedAt(currentTimestamp)
				.expiration(expirationTimestamp)
				.audience().add(audience)
				.and()
				.signWith(secretKey, Jwts.SIG.HS256)
				.compact();
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

	/**
	 * Extracts a specific claim from the provided JWT token. This method verifies
	 * the token's issuer and signature before extracting the claim.
	 * 
	 * @param token JWT token from which the desired claim is to be extracted.
	 * @param claimsResolver function of {@link Claims} to execute. example: {@code Claims::getId}.
	 * @throws IllegalArgumentException if any provided argument is <code>null</code>
	 * @return The extracted claim from the JWT token.
	 */
	private <T> T extractClaim(@NonNull final String token, @NonNull final Function<Claims, T> claimsResolver) {
		final var encodedSecretKey = tokenConfigurationProperties.getAccessToken().getSecretKey();
		final var secretKey = getSecretKey(encodedSecretKey);
		final var sanitizedToken = token.replace(BEARER_PREFIX, StringUtils.EMPTY);
		final var claims = Jwts.parser()
				.requireIssuer(issuer)
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(sanitizedToken)
				.getPayload();
		return claimsResolver.apply(claims);
	}

	/**
	 * Constructs an instance of {@link SecretKey} from the provided Base64-encoded
	 * secret key string.
	 * 
	 * @param encodedKey The Base64-encoded secret key string.
	 * @throws IllegalArgumentException if encodedKey is <code>null</code>
	 * @return A {@link SecretKey} instance for JWT signing and verification.
	 */
	private SecretKey getSecretKey(@NonNull final String encodedKey) {
		final var decodedKey = Decoders.BASE64.decode(encodedKey);
		return Keys.hmacShaKeyFor(decodedKey);
	}

}