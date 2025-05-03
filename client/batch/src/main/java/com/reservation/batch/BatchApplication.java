package com.reservation.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.reservation.batch",
	"com.reservation.domain",
	"com.reservation.support",
	"com.reservation.querysupport",
})
public class BatchApplication {
	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}
}
