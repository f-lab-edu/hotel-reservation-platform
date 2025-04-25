package com.reservation.host;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.reservation.auth",
	"com.reservation.domain",
	"com.reservation.support",
	"com.reservation.querysupport",
	"com.reservation.host",
})
public class HostApplication {
	public static void main(String[] args) {
		SpringApplication.run(HostApplication.class, args);
	}
}
