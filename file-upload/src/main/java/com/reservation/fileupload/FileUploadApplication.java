package com.reservation.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
	"com.reservation.fileupload",
	"com.reservation.common",
	"com.reservation.commonauth",
})
public class FileUploadApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileUploadApplication.class, args);
	}
}
