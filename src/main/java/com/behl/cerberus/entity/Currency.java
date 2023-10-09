package com.behl.cerberus.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Currency {

	USD("United States Dollar");

	private final String value;

}