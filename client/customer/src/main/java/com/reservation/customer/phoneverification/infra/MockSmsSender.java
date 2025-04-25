package com.reservation.customer.phoneverification.infra;

import org.springframework.stereotype.Component;

import com.reservation.customer.phoneverification.service.SmsSender;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class MockSmsSender implements SmsSender {
	@Override
	public void send(String phoneNumber, String message) {
		log.info("[MOCK SMS] To: {}, Message: {}", phoneNumber, message);
	}
}
