package com.reservation.host.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = {"com.reservation.domain", "com.reservation.host"})
@EnableJpaRepositories(basePackages = "com.reservation.host")
public class JpaConfig {
}
