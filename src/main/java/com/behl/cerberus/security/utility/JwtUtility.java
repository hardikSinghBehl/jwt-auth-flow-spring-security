package com.behl.cerberus.security.utility;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.behl.cerberus.configuration.properties.JwtConfigurationProperties;
import com.behl.cerberus.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@EnableConfigurationProperties(JwtConfigurationProperties.class)
@RequiredArgsConstructor
public class JwtUtility {

	private final JwtConfigurationProperties jwtConfigurationProperties;
	private final HttpServletRequest httpRequest;

	@Value("${spring.application.name}")
	private String issuer;

	public UUID extractUserId(@NonNull final String token) {
		return UUID.fromString((String) extractAllClaims(token).get("user_id"));
	}

	public LocalDateTime extractExpirationTimestamp(@NonNull final String token) {
		return extractClaim(token, Claims::getExpiration).toInstant().atZone(ZoneId.systemDefault())
				.withZoneSameInstant(ZoneId.of("+00:00")).toLocalDateTime();
	}

	public String generateAccessToken(@NonNull final User user) {
		final Claims claims = new DefaultClaims();
		claims.put("user_id", user.getId());
		claims.put("account_creation_timestamp",
				user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
		claims.put("name", user.getFirstName() + " " + user.getLastName());
		return createToken(claims, user.getEmailId(),
				TimeUnit.MINUTES.toMillis(jwtConfigurationProperties.getJwt().getAccessToken().getValidity()));
	}

	public String generateRefreshToken(@NonNull final User user) {
		final Claims claims = new DefaultClaims();
		claims.put("user_id", user.getId());
		return createToken(claims, user.getEmailId(),
				TimeUnit.DAYS.toMillis(jwtConfigurationProperties.getJwt().getRefreshToken().getValidity()));
	}

	public Boolean validateToken(@NonNull final String token, @NonNull final UserDetails user) {
		final String emailId = extractClaim(token, Claims::getSubject);
		return (emailId.equals(user.getUsername()) && !isTokenExpired(token));
	}

	public Boolean isTokenExpired(@NonNull final String token) {
		final var tokenExpirationDate = extractClaim(token, Claims::getExpiration);
		return tokenExpirationDate.before(new Date());
	}

	private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Claims extractAllClaims(final String token) {
		return Jwts.parser().setSigningKey(jwtConfigurationProperties.getJwt().getSecretKey())
				.parseClaimsJws(token.replace("Bearer ", "")).getBody();
	}

	private String createToken(final Claims claims, final String subject, final Long expiration) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration)).setId(UUID.randomUUID().toString())
				.setAudience(httpRequest.getRemoteHost()).setIssuer(issuer)
				.signWith(SignatureAlgorithm.HS256, jwtConfigurationProperties.getJwt().getSecretKey()).compact();
	}

}