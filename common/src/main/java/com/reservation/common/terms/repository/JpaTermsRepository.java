package com.reservation.common.terms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.common.terms.domain.Terms;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;

public interface JpaTermsRepository extends JpaRepository<Terms, Long> {

	boolean existsByCodeAndStatus(TermsCode code, TermsStatus status);

	@Query("SELECT MAX(t.version) FROM Terms t WHERE t.code = :code")
	Optional<Integer> findMaxVersionByCode(@Param("code") TermsCode code);
}
