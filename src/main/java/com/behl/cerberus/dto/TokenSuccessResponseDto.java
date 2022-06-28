package com.behl.cerberus.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenSuccessResponseDto implements Serializable {

	private static final long serialVersionUID = -8752513311904244663L;

	private final String accessToken;
	private final String refreshToken;
	private final LocalDateTime expiresAt;

}
