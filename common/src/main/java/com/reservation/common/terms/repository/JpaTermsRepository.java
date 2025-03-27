package com.reservation.common.terms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.terms.domain.Terms;

public interface JpaTermsRepository extends JpaRepository<Terms, Long> {
}
