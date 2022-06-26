package com.behl.cerberus.security.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GetApiPathExclusion {

	HEALTH_CHECK("/ping");

	private final String path;
}