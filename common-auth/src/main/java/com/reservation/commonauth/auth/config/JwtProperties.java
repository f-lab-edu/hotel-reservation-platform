package com.reservation.commonauth.auth.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private String secretKey;
	private long accessTokenExpiry;
	private long refreshTokenExpiry;
	private List<String> skipUrls;
}
