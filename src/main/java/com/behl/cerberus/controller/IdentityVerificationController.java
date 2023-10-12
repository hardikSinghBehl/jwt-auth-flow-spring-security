package com.behl.cerberus.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.behl.cerberus.dto.IdentityVerificationRequestDto;
import com.behl.cerberus.service.IdentityVerificationService;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class IdentityVerificationController {

	private final IdentityVerificationService identityVerificationService;
	private final AuthenticatedUserIdProvider authenticatedUserIdProvider;
	
	@PostMapping(value = "/identity-verification", consumes = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Verify user identity", description = "Performs identity verification check on logged-in user")
	@ApiResponse(responseCode = "200", description = "User identity verification check successfully passed")
	@PreAuthorize("hasAuthority('useridentity.verify')")
	public ResponseEntity<HttpStatus> verifyUserIdentity(@Valid @RequestBody final IdentityVerificationRequestDto identityVerificationRequest) {
		final var userId = authenticatedUserIdProvider.getUserId();
		identityVerificationService.verifyUserIdentity(userId, identityVerificationRequest);
		return ResponseEntity.ok().build();
	}

}
