package com.reservation.customer.terms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.domain.terms.enums.TermsType;

public interface JpaTermsRepository extends JpaRepository<Terms, Long> {
	List<Terms> findByTypeAndStatus(TermsType termsType, TermsStatus termsStatus);

	List<Terms> findByStatusAndIsLatest(TermsStatus termsStatus, Boolean isLatest);
}
