package com.reservation.admin.terms.controller.request

import com.reservation.domain.terms.Clause
import com.reservation.domain.terms.Terms

@JvmRecord
data class UpdateClauseRequest(
    val clauseOrder: Int?,
    val title: String?,
    val content: String?
) {
    fun validToClause(updateTerms: Terms): Clause {
        require(!(clauseOrder == null || clauseOrder <= 0)) { "조항 순서는 0보다 커야 합니다." }
        require(!(title == null || title.isBlank())) { "조항 제목은 필수입니다." }
        require(!(content == null || content.isBlank())) { "조항 내용은 필수입니다." }
        requireNotNull(updateTerms) { "약관이 필요합니다." }

        return Clause.builder()
            .terms(updateTerms)
            .clauseOrder(clauseOrder)
            .title(title)
            .content(content)
            .build()
    }
}
