package com.behl.cerberus.configuration;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.behl.cerberus.unsecured.api-path")
public class ApiPathExclusionConfigurationProperties {
	
	private boolean swaggerV3;
	
	private List<String> get = new ArrayList<String>();
	private List<String> post = new ArrayList<String>();
	private List<String> put = new ArrayList<String>();

}
