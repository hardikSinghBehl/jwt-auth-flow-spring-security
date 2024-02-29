package com.behl.cerberus.utility;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import io.jsonwebtoken.io.Decoders;
import lombok.NonNull;
import lombok.SneakyThrows;

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
				.signWith(getPrivateKey(), Jwts.SIG.RS512)
				.compact();
	}
	
	/**
	 * Extracts user's ID from a given JWT token signifying an authenticated
	 * user.
	 * 
	 * @param token The JWT token from which to extract the user's ID.
	 * @throws IllegalArgumentException if provided argument is <code>null</code>.
	 * @return The authenticated user's unique identifier (ID) in UUID format.
	 */
	public UUID extractUserId(@NonNull final String token) {
		final var audience = extractClaim(token, Claims::getAudience).iterator().next();
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
	 * the token's issuer and signature using the configured public key 
	 * before extracting the claim.
	 * 
	 * @param token JWT token from which the desired claim is to be extracted.
	 * @param claimsResolver function of {@link Claims} to execute. example: {@code Claims::getId}.
	 * @throws IllegalArgumentException if any provided argument is <code>null</code>
	 * @return The extracted claim from the JWT token.
	 */
	private <T> T extractClaim(@NonNull final String token, @NonNull final Function<Claims, T> claimsResolver) {
		final var sanitizedToken = token.replace(BEARER_PREFIX, StringUtils.EMPTY);
		final var claims = Jwts.parser()
				.requireIssuer(issuer)
				.verifyWith(getPublicKey())
				.build()
				.parseSignedClaims(sanitizedToken)
				.getPayload();
		return claimsResolver.apply(claims);
	}
	
	/**
	 * Retrieves the configured RSA private key and converts it into
	 * {@link PrivateKey} object to be used for JWT signing. The configured private
	 * key must be in PKCS#8 format.
	 * 
	 * @return {@link PrivateKey} object for signing generated JWT token.
	 */
	@SneakyThrows
	private PrivateKey getPrivateKey() {
		final var privateKey = tokenConfigurationProperties.getAccessToken().getPrivateKey();
		final var sanitizedPrivateKey = sanitizeKey(privateKey);

		final var decodedPrivateKey = Decoders.BASE64.decode(sanitizedPrivateKey);
		final var spec = new PKCS8EncodedKeySpec(decodedPrivateKey);
		
		return KeyFactory.getInstance("RSA").generatePrivate(spec);
	}
	
	/**
	 * Retrieves the configured RSA public key and converts it into
	 * {@link PublicKey} object to be used for JWT signature verification.
	 * 
	 * @return {@link PublicKey} object for verifying JWT signature.
	 */
	@SneakyThrows
	private PublicKey getPublicKey() {
		final var publicKey = tokenConfigurationProperties.getAccessToken().getPublicKey();
		final var sanitizedPublicKey = sanitizeKey(publicKey);
		
		final var decodedPublicKey = Decoders.BASE64.decode(sanitizedPublicKey);
		final var spec = new X509EncodedKeySpec(decodedPublicKey);
		
		return KeyFactory.getInstance("RSA").generatePublic(spec);
	}
	
	/**
	 * Sanitizes the provided key by removing header, footer and new line 
	 * separators. The method works for both public and private key pairs.
	 * 
	 * @param key The key to be sanitized.
	 * @throws IllegalArgumentException if the provided key is <code>null</code>.
	 * @return The sanitized key as a single string.
	 */
	private String sanitizeKey(@NonNull final String key) {
		return key
	    		.replace("-----BEGIN PUBLIC KEY-----", StringUtils.EMPTY)
				.replace("-----END PUBLIC KEY-----", StringUtils.EMPTY)
				.replace("-----BEGIN PRIVATE KEY-----", StringUtils.EMPTY)
				.replace("-----END PRIVATE KEY-----", StringUtils.EMPTY)
				.replaceAll("\\n", StringUtils.EMPTY)
				.replaceAll("\\s", StringUtils.EMPTY);
	}

}