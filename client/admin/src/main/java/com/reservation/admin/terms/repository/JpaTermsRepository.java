package com.reservation.admin.terms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.reservation.domain.terms.Terms;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.domain.terms.enums.TermsStatus;

public interface JpaTermsRepository extends JpaRepository<Terms, Long> {

	boolean existsByCodeAndStatus(TermsCode code, TermsStatus status);

	@Query("SELECT MAX(t.version) FROM Terms t WHERE t.code = :code")
	Optional<Integer> findMaxVersionByCode(@Param("code") TermsCode code);
}
