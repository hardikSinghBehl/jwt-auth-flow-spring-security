package com.behl.cerberus.security.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PutApiPathExclusion {

	REFRESH_TOKEN("/auth/refresh");

	private final String path;
}