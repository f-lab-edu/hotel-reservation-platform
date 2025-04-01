package com.reservation.commonapi.terms.repository.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;

import javax.annotation.processing.Generated;

/**
 * com.reservation.commonapi.terms.repository.dto.QTermsDto is a Querydsl Projection type for TermsDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QTermsDto extends ConstructorExpression<TermsDto> {

	private static final long serialVersionUID = -543314570L;

	public QTermsDto(com.querydsl.core.types.Expression<Long> id,
		com.querydsl.core.types.Expression<com.reservation.commonmodel.terms.TermsCode> code,
		com.querydsl.core.types.Expression<String> title,
		com.querydsl.core.types.Expression<com.reservation.commonmodel.terms.TermsType> type,
		com.querydsl.core.types.Expression<com.reservation.commonmodel.terms.TermsStatus> status,
		com.querydsl.core.types.Expression<Integer> version, com.querydsl.core.types.Expression<Boolean> isLatest,
		com.querydsl.core.types.Expression<java.time.LocalDateTime> exposedFrom,
		com.querydsl.core.types.Expression<java.time.LocalDateTime> exposedToOrNull,
		com.querydsl.core.types.Expression<Integer> displayOrder,
		com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt,
		com.querydsl.core.types.Expression<java.time.LocalDateTime> updatedAt,
		com.querydsl.core.types.Expression<? extends java.util.List<ClauseDto>> clauses) {
		super(TermsDto.class,
			new Class<?>[] {long.class, com.reservation.commonmodel.terms.TermsCode.class, String.class,
				com.reservation.commonmodel.terms.TermsType.class, com.reservation.commonmodel.terms.TermsStatus.class,
				int.class, boolean.class, java.time.LocalDateTime.class, java.time.LocalDateTime.class, int.class,
				java.time.LocalDateTime.class, java.time.LocalDateTime.class, java.util.List.class}, id, code, title,
			type, status, version, isLatest, exposedFrom, exposedToOrNull, displayOrder, createdAt, updatedAt, clauses);
	}

}

