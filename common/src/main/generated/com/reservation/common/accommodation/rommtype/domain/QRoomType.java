package com.reservation.common.accommodation.rommtype.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoomType is a Querydsl query type for RoomType
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoomType extends EntityPathBase<RoomType> {

    private static final long serialVersionUID = 1441907928L;

    public static final QRoomType roomType = new QRoomType("roomType");

    public final com.reservation.common.domain.QBaseEntity _super = new com.reservation.common.domain.QBaseEntity(this);

    public final NumberPath<Long> accommodationId = createNumber("accommodationId", Long.class);

    public final NumberPath<Integer> capacity = createNumber("capacity", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath descriptionOrNull = createString("descriptionOrNull");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath name = createString("name");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final NumberPath<Integer> roomCount = createNumber("roomCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoomType(String variable) {
        super(RoomType.class, forVariable(variable));
    }

    public QRoomType(Path<? extends RoomType> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoomType(PathMetadata metadata) {
        super(RoomType.class, metadata);
    }

}

