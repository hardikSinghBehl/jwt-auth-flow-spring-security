package com.behl.cerberus.dto;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserLoginRequestDto implements Serializable {

	private static final long serialVersionUID = -3243809542585061706L;

	@NotBlank(message = "email-id must not be empty")
	@Email(message = "email-id must be of valid format")
	@Schema(required = true, name = "emailId", example = "hardik.behl7444@gmail.com", description = "email-id associated with user account already created in the system")
	private final String emailId;

	@NotBlank(message = "password must not be empty")
	@Schema(required = true, example = "somethingSecure", description = "password corresponding to provided email-id")
	private final String password;

}
