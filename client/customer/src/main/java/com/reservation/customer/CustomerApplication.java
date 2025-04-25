package com.reservation.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.reservation.customer",
	"com.reservation.domain",
	"com.reservation.auth",
	"com.reservation.support",
	"com.reservation.querysupport",
})
public class CustomerApplication {
	public static void main(String[] args) {
		SpringApplication.run(CustomerApplication.class, args);
	}
}
