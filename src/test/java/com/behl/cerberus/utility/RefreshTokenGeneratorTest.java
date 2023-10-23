package com.behl.cerberus.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import lombok.SneakyThrows;

class RefreshTokenGeneratorTest {

	private final RefreshTokenGenerator refreshTokenGenerator = new RefreshTokenGenerator();

	@Test
	void shouldGenerateUniqueNonEmptyRefreshTokens() {
		// Generating refresh tokens in bulk
		final var tokenBatchSize = 100;
		final List<String> refreshTokens = new ArrayList<String>();
		for (int i = 0; i < tokenBatchSize; i++) {
			final var refreshToken = refreshTokenGenerator.generate();
			refreshTokens.add(refreshToken);
		}

		// Assert that the generated tokens are unique, non-null, and non-empty
		assertThat(refreshTokens)
			.doesNotHaveDuplicates()
			.doesNotContainNull()
			.doesNotContain(StringUtils.EMPTY);
	}

	@Test
	@SneakyThrows
	void shouldGenerateRefreshTokensWithExpectedLength() {
	    // Calculate the expected length of a refresh token 
		// (in hexadecimal form, * 2 for byte-to-hex conversion)
		final var expectedLength = MessageDigest.getInstance("SHA256").getDigestLength() * 2;
		
		// Generate a refresh token
		final var refreshToken = refreshTokenGenerator.generate();
		
		// Assert that the generated refresh token has the expected length
		assertThat(refreshToken).hasSize(expectedLength);
	}

}