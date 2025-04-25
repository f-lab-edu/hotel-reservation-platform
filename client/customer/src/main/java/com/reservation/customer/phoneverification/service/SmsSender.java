package com.reservation.customer.phoneverification.service;

public interface SmsSender {
	void send(String phoneNumber, String message);
}
