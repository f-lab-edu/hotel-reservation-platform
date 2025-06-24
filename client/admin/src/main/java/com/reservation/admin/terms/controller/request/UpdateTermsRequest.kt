package com.reservation.admin.terms.controller.request

import com.reservation.domain.terms.Terms
import com.reservation.domain.terms.enums.TermsCode
import com.reservation.domain.terms.enums.TermsStatus
import com.reservation.domain.terms.enums.TermsType
import com.reservation.support.exception.ErrorCode
import java.time.LocalDateTime

@JvmRecord
data class UpdateTermsRequest(
    val id: Long,
    val code: TermsCode?,
    val title: String?,
    val type: TermsType?,
    val status: TermsStatus?,
    val exposedFrom: LocalDateTime?,
    val exposedToOrNull: LocalDateTime?,
    val displayOrder: Int?,
    val clauses: List<UpdateClauseRequest>?
) {
    fun validToTerms(termsId: Long): Terms {
        if (termsId < 0) {
            throw ErrorCode.BAD_REQUEST.exception("유효하지 않은 약관 ID 입니다.")
        }
        if (code == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 코드가 비어있습니다.")
        }
        if (title == null || title.isBlank()) {
            throw ErrorCode.BAD_REQUEST.exception("약관 제목이 비어있습니다.")
        }
        if (type == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 타입이 비어있습니다.")
        }
        if (status == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 상태가 비어있습니다.")
        }
        if (exposedFrom == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관 노출 시작일이 비어있습니다.")
        }
        if (displayOrder == null || displayOrder < 1) {
            throw ErrorCode.BAD_REQUEST.exception("약관 노출 순서가 비어있습니다.")
        }
        if (exposedToOrNull != null && exposedFrom.isAfter(exposedToOrNull)) {
            throw ErrorCode.BAD_REQUEST.exception("약관 노출 종료일은 노출 시작일보다 늦어야 합니다.")
        }
        if (clauses == null || clauses.isEmpty()) {
            throw ErrorCode.BAD_REQUEST.exception("약관 조항이 비어있습니다.")
        }

        val updateTerms = Terms.builder()
            .id(termsId)
            .code(code)
            .title(title)
            .type(type)
            .status(status)
            .exposedFrom(exposedFrom)
            .exposedToOrNull(exposedToOrNull)
            .displayOrder(displayOrder)
            .build()

        val clause = clauses.stream()
            .map { request: UpdateClauseRequest -> request.validToClause(updateTerms) }
            .toList()

        return updateTerms
    }
}
