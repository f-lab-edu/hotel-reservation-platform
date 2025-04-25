package com.reservation.fileupload.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "com.reservation.domain")
@EnableJpaRepositories(basePackages = "com.reservation.fileupload")
public class JpaConfig {

}
