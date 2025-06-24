package com.reservation.admin.terms.repository

import com.reservation.domain.terms.Terms
import com.reservation.domain.terms.enums.TermsCode
import com.reservation.domain.terms.enums.TermsStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface JpaTermsRepository : JpaRepository<Terms, Long> {
    fun existsByCodeAndStatus(code: TermsCode, status: TermsStatus): Boolean

    @Query("SELECT MAX(t.version) FROM Terms t WHERE t.code = :code")
    fun findMaxVersionByCode(@Param("code") code: TermsCode): Optional<Int>
}
