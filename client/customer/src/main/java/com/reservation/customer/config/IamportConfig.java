package com.reservation.customer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.siot.IamportRestClient.IamportClient;

@Configuration
public class IamportConfig {
	@Value("${iamport.api-key}")
	protected String apiKey;
	@Value("${iamport.api-secret}")
	protected String apiSecret;

	@Bean
	public IamportClient iamportClient() {
		return new IamportClient(apiKey, apiSecret);
	}
}
