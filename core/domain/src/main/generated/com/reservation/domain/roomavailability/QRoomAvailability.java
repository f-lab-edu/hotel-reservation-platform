package com.reservation.domain.roomavailability;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoomAvailability is a Querydsl query type for RoomAvailability
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoomAvailability extends EntityPathBase<RoomAvailability> {

    private static final long serialVersionUID = -862601549L;

    public static final QRoomAvailability roomAvailability = new QRoomAvailability("roomAvailability");

    public final com.reservation.domain.base.QBaseEntity _super = new com.reservation.domain.base.QBaseEntity(this);

    public final NumberPath<Integer> availableCount = createNumber("availableCount", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> roomId = createNumber("roomId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoomAvailability(String variable) {
        super(RoomAvailability.class, forVariable(variable));
    }

    public QRoomAvailability(Path<? extends RoomAvailability> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoomAvailability(PathMetadata metadata) {
        super(RoomAvailability.class, metadata);
    }

}

