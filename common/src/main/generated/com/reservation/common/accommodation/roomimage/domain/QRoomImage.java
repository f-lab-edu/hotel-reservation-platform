package com.reservation.common.accommodation.roomimage.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QRoomImage is a Querydsl query type for RoomImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRoomImage extends EntityPathBase<RoomImage> {

    private static final long serialVersionUID = 490198516L;

    public static final QRoomImage roomImage = new QRoomImage("roomImage");

    public final com.reservation.common.domain.QBaseEntity _super = new com.reservation.common.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath imageUrl = createString("imageUrl");

    public final BooleanPath isMainImage = createBoolean("isMainImage");

    public final NumberPath<Long> roomTypeId = createNumber("roomTypeId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QRoomImage(String variable) {
        super(RoomImage.class, forVariable(variable));
    }

    public QRoomImage(Path<? extends RoomImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRoomImage(PathMetadata metadata) {
        super(RoomImage.class, metadata);
    }

}

