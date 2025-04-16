package com.reservation.host;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.reservation.host",
	"com.reservation.common",
	"com.reservation.commonauth",
})
public class HostApplication {
	public static void main(String[] args) {
		SpringApplication.run(HostApplication.class, args);
	}
}
