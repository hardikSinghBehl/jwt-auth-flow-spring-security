package com.behl.cerberus.utility;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.behl.cerberus.configuration.JwtConfigurationProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtConfigurationProperties.class)
public class JwtUtility {

	private final JwtConfigurationProperties jwtConfigurationProperties;
	
	private static final String BEARER_PREFIX = "Bearer ";

	@Value("${spring.application.name}")
	private String issuer;

	public UUID extractUserId(@NonNull final String token) {
		final var audience = extractClaim(token, Claims::getAudience);
		return UUID.fromString(audience);
	}

	public String generateAccessToken(@NonNull final UUID userId) {
		final var accessTokenValidity = jwtConfigurationProperties.getJwt().getAccessToken().getValidity();
		return createToken(userId, TimeUnit.MINUTES.toMillis(accessTokenValidity));
	}

	public String generateRefreshToken(@NonNull final UUID userId) {
		final var refreshTokenValidity = jwtConfigurationProperties.getJwt().getRefreshToken().getValidity();
		return createToken(userId, TimeUnit.DAYS.toMillis(refreshTokenValidity));
	}

	public Boolean validateToken(@NonNull final String token, @NonNull final UUID userId) {
		final var audience = extractClaim(token, Claims::getAudience);
		return UUID.fromString(audience).equals(userId) && !isTokenExpired(token);
	}

	public Boolean isTokenExpired(@NonNull final String token) {
		final var tokenExpirationDate = extractClaim(token, Claims::getExpiration);
		return tokenExpirationDate.before(new Date(System.currentTimeMillis()));
	}
	
	public LocalDateTime getExpirationTimestamp(@NonNull final String token) {
		final var expiration = extractClaim(token, Claims::getExpiration);
		return expiration.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
	}

	private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
		final var secretKey = jwtConfigurationProperties.getJwt().getSecretKey();
		final var santizedToken = token.replace(BEARER_PREFIX, StringUtils.EMPTY);
		final var claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(santizedToken).getBody();
		return claimsResolver.apply(claims);
	}

	private String createToken(final UUID userId, final Long expiration) {
		final var secretKey = jwtConfigurationProperties.getJwt().getSecretKey();
		final var currentTimestamp = new Date(System.currentTimeMillis());
		final var expirationTimestamp = new Date(System.currentTimeMillis() + expiration);
		
		return Jwts.builder()
				.setIssuer(issuer)
				.setIssuedAt(currentTimestamp)
				.setExpiration(expirationTimestamp)
				.setAudience(String.valueOf(userId))
				.signWith(SignatureAlgorithm.HS256, secretKey).compact();
	}

}