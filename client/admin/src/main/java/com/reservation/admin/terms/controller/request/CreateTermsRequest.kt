package com.reservation.admin.terms.controller.request

import com.reservation.domain.terms.Terms
import com.reservation.domain.terms.enums.TermsCode
import com.reservation.domain.terms.enums.TermsStatus
import com.reservation.domain.terms.enums.TermsType
import com.reservation.support.exception.ErrorCode
import java.time.LocalDateTime

@JvmRecord
data class CreateTermsRequest(
    val code: TermsCode?,
    val title: String?,
    val type: TermsType?,
    val exposedFrom: LocalDateTime?,
    val exposedToOrNull: LocalDateTime,
    val displayOrder: Int?,
    val clauses: List<CreateClauseRequest>?
) {
    fun validToTerms(): Terms {
        if (code == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 코드가 필요합니다.")
        }
        if (title == null || title.isBlank()) {
            throw ErrorCode.BAD_REQUEST.exception("약관 제목이 필요합니다.")
        }
        if (type == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 타입이 필요합니다.")
        }
        if (exposedFrom == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 노출 시작일이 필요합니다.")
        }
        if (displayOrder == null || displayOrder < 0) {
            throw ErrorCode.BAD_REQUEST.exception("약관 노출 순서는 0 이상이어야 합니다.")
        }
        if (clauses == null || clauses.isEmpty()) {
            throw ErrorCode.BAD_REQUEST.exception("약관 조항이 필요합니다.")
        }

        val newTerms = Terms.builder()
            .code(code)
            .title(title)
            .type(type)
            .status(TermsStatus.ACTIVE)
            .exposedFrom(exposedFrom)
            .displayOrder(displayOrder)
            .build()

        val clauses =
            clauses.stream().map { request: CreateClauseRequest -> request.validToClause(newTerms) }.toList()

        newTerms.clauses = clauses
        return newTerms
    }
}
