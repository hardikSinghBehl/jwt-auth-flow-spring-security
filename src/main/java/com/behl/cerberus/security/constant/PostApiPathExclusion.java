package com.behl.cerberus.security.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostApiPathExclusion {

	SIGN_UP("/users"), LOGIN("/auth/login");

	private final String path;
}