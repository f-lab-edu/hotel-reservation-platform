package com.reservation.admin.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/health")
@Tag(name = "헬스 체크 API")
public class HealthController {
	@GetMapping()
	public String health() {
		return "Third - OK";
	}
}
