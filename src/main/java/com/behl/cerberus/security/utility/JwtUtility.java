package com.behl.cerberus.security.utility;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.behl.cerberus.configuration.properties.JwtConfigurationProperties;
import com.behl.cerberus.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Component
@EnableConfigurationProperties(JwtConfigurationProperties.class)
@RequiredArgsConstructor
public class JwtUtility {

	private final JwtConfigurationProperties jwtConfigurationProperties;

	public String extractEmail(final String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public UUID extractUserId(final String token) {
		return UUID.fromString((String) extractAllClaims(token).get("user_id"));
	}

	public String generateAccessToken(final User user) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("user_id", user.getId());
		claims.put("account_creation_timestamp", user.getCreatedAt());
		claims.put("name", user.getFirstName() + " " + user.getLastName());
		return createToken(claims, user.getEmailId(), TimeUnit.MINUTES.toMillis(30));
	}

	public String generateRefreshToken(final User user) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, user.getEmailId(), TimeUnit.DAYS.toMillis(15));
	}

	public Boolean validateToken(final String token, final UserDetails user) {
		final String emailId = extractEmail(token);
		return (emailId.equals(user.getUsername()) && !isTokenExpired(token));
	}

	private Claims extractAllClaims(final String token) {
		return Jwts.parser().setSigningKey(jwtConfigurationProperties.getJwt().getSecretKey())
				.parseClaimsJws(token.replace("Bearer ", "")).getBody();
	}

	private Boolean isTokenExpired(final String token) {
		return extractExpiration(token).before(new Date());
	}

	private <T> T extractClaim(final String token, final Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	private Date extractExpiration(final String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private String createToken(final Map<String, Object> claims, final String subject, final Long expiration) {
		return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(SignatureAlgorithm.HS256, jwtConfigurationProperties.getJwt().getSecretKey()).compact();
	}

}