package com.behl.cerberus.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class AuthenticatedUserIdProviderTest {

	private final AuthenticatedUserIdProvider authenticatedUserIdProvider = new AuthenticatedUserIdProvider();

	@Test
	void shouldExtractUserIdOfAuthenticatedUserFromSecurityPrincipal() {
		// preparing security context to hold userId as principal 
		final var userId = UUID.randomUUID();
		final var securityContext = mock(SecurityContext.class);
		final var authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userId);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		final var response = authenticatedUserIdProvider.getUserId();

		assertThat(response).isInstanceOf(UUID.class).isEqualTo(userId);
		verify(securityContext).getAuthentication();
		verify(authentication).getPrincipal();
	}

	@Test
	void shouldThrowIllegalStateExceptionForMissingSecurityContext() {
		SecurityContextHolder.clearContext();

		assertThrows(IllegalStateException.class, authenticatedUserIdProvider::getUserId);
	}

	@Test
	void shouldThrowIllegalStateExceptionWhenPrincipalNonUUID() {
		// preparing security context to hold UserDetails as principal 
		final var userDetails = mock(UserDetails.class);
		final var securityContext = mock(SecurityContext.class);
		final var authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);
		when(securityContext.getAuthentication()).thenReturn(authentication);
		SecurityContextHolder.setContext(securityContext);

		assertThrows(IllegalStateException.class, authenticatedUserIdProvider::getUserId);
	}

}