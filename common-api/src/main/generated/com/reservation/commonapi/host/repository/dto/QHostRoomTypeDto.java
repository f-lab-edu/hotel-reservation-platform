package com.reservation.commonapi.host.repository.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.reservation.commonapi.host.repository.dto.QHostRoomTypeDto is a Querydsl Projection type for HostRoomTypeDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QHostRoomTypeDto extends ConstructorExpression<HostRoomTypeDto> {

    private static final long serialVersionUID = 1082009447L;

    public QHostRoomTypeDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<Long> accommodationId, com.querydsl.core.types.Expression<String> name, com.querydsl.core.types.Expression<Integer> capacity, com.querydsl.core.types.Expression<Integer> price, com.querydsl.core.types.Expression<String> descriptionOrNull, com.querydsl.core.types.Expression<Integer> roomCount, com.querydsl.core.types.Expression<String> mainImageUrl) {
        super(HostRoomTypeDto.class, new Class<?>[]{long.class, long.class, String.class, int.class, int.class, String.class, int.class, String.class}, id, accommodationId, name, capacity, price, descriptionOrNull, roomCount, mainImageUrl);
    }

}

