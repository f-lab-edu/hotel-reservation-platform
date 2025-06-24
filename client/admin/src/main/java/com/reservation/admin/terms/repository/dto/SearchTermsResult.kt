package com.reservation.admin.terms.repository.dto

import com.querydsl.core.annotations.QueryProjection
import com.reservation.domain.terms.enums.TermsCode
import com.reservation.domain.terms.enums.TermsStatus
import com.reservation.domain.terms.enums.TermsType
import lombok.Getter
import java.time.LocalDateTime

@Getter
class SearchTermsResult @QueryProjection constructor(
    var id: Long,
    var code: TermsCode,
    var title: String,
    var type: TermsType,
    var status: TermsStatus,
    var version: Int,
    var isLatest: Boolean,
    var exposedFrom: LocalDateTime,
    var exposedToOrNull: LocalDateTime,
    var displayOrder: Int,
    var createdAt: LocalDateTime
)
