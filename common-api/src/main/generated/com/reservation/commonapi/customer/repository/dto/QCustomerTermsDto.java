package com.reservation.commonapi.customer.repository.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.reservation.commonapi.customer.repository.dto.QCustomerTermsDto is a Querydsl Projection type for CustomerTermsDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QCustomerTermsDto extends ConstructorExpression<CustomerTermsDto> {

    private static final long serialVersionUID = 48522599L;

    public QCustomerTermsDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<com.reservation.commonmodel.terms.TermsCode> code, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<com.reservation.commonmodel.terms.TermsType> type, com.querydsl.core.types.Expression<com.reservation.commonmodel.terms.TermsStatus> status, com.querydsl.core.types.Expression<Integer> version, com.querydsl.core.types.Expression<java.time.LocalDateTime> exposedFrom, com.querydsl.core.types.Expression<Integer> displayOrder) {
        super(CustomerTermsDto.class, new Class<?>[]{long.class, com.reservation.commonmodel.terms.TermsCode.class, String.class, com.reservation.commonmodel.terms.TermsType.class, com.reservation.commonmodel.terms.TermsStatus.class, int.class, java.time.LocalDateTime.class, int.class}, id, code, title, type, status, version, exposedFrom, displayOrder);
    }

}

