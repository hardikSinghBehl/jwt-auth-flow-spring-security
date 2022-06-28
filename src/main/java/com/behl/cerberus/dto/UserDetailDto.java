package com.behl.cerberus.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserDetailDto {

	private final String firstName;
	private final String lastName;
	private final String emailId;
	private final LocalDateTime createdAt;

}
