package com.behl.cerberus.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
public class RefreshTokenGenerator {
	
    private static final String ALGORITHM = "SHA256";

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
