package com.reservation.admin.terms.controller.request

import com.reservation.domain.terms.Clause
import com.reservation.domain.terms.Terms
import com.reservation.support.exception.ErrorCode

@JvmRecord
data class CreateClauseRequest(
    val clauseOrder: Int?,
    val title: String?,
    val content: String?
) {
    fun validToClause(terms: Terms): Clause {
        if (clauseOrder == null || clauseOrder <= 0) {
            throw ErrorCode.BAD_REQUEST.exception("조항 순서는 0보다 커야 합니다.")
        }
        if (title == null || title.isBlank()) {
            throw ErrorCode.BAD_REQUEST.exception("조항 제목은 필수입니다.")
        }
        if (content == null || content.isBlank()) {
            throw ErrorCode.BAD_REQUEST.exception("조항 내용은 필수입니다.")
        }
        if (terms == null) {
            throw ErrorCode.BAD_REQUEST.exception("약관이 필요합니다.")
        }

        return Clause.builder()
            .terms(terms)
            .clauseOrder(clauseOrder)
            .title(title)
            .content(content)
            .build()
    }
}
