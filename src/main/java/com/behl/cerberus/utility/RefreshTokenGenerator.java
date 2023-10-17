package com.behl.cerberus.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

/**
 * Utility class responsible for generating refresh tokens. These tokens are used
 * to facilitate secure and time-limited re-authentication within the system.
 * 
 * @see com.behl.cerberus.service.AuthenticationService
 */
@Component
public class RefreshTokenGenerator {
	
    private static final String ALGORITHM = "SHA256";

	/**
	 * @return A randomly generated refresh token that is unique to each invocation.
	 */
	@SneakyThrows
	public String generate() {
		final var randomIdentifier = String.valueOf(UUID.randomUUID());
        final var messageDigest = MessageDigest.getInstance(ALGORITHM);
        final var hash = messageDigest.digest(randomIdentifier.getBytes(StandardCharsets.UTF_8));
        return convertBytesToString(hash);
	}
	
    private String convertBytesToString(final byte[] bytes) {
        final var hexStringBuilder = new StringBuilder();
        for (final byte currentByte : bytes) {
            final var hexValue = String.format("%02x", currentByte);
            hexStringBuilder.append(hexValue);
        }
        return hexStringBuilder.toString();
    }

}
