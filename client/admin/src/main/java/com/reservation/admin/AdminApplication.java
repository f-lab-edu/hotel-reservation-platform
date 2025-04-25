package com.reservation.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.reservation.admin",
	"com.reservation.auth",
	"com.reservation.domain",
	"com.reservation.querysupport",
	"com.reservation.support",
})
public class AdminApplication {
	public static void main(String[] args) {
		SpringApplication.run(AdminApplication.class, args);
	}
}
