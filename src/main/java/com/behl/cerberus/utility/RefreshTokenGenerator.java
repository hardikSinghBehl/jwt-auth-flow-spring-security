package com.behl.cerberus.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshTokenGenerator {
	
    private static final String ALGORITHM = "SHA256";

	@SneakyThrows
	public static String generate() {
		final var randomIdentifier = String.valueOf(UUID.randomUUID());
        final var messageDigest = MessageDigest.getInstance(ALGORITHM);
        final var hash = messageDigest.digest(randomIdentifier.getBytes(StandardCharsets.UTF_8));
        return convertBytesToString(hash);
	}
	
    private static String convertBytesToString(final byte[] bytes) {
        final var hexStringBuilder = new StringBuilder();
        for (final byte currentByte : bytes) {
            final var hexValue = String.format("%02x", currentByte);
            hexStringBuilder.append(hexValue);
        }
        return hexStringBuilder.toString();
    }

}
