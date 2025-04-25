package com.reservation.domain.accommodation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAccommodation is a Querydsl query type for Accommodation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAccommodation extends EntityPathBase<Accommodation> {

    private static final long serialVersionUID = 163355355L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAccommodation accommodation = new QAccommodation("accommodation");

    public final com.reservation.domain.base.QBaseEntity _super = new com.reservation.domain.base.QBaseEntity(this);

    public final StringPath contactNumber = createString("contactNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath descriptionOrNull = createString("descriptionOrNull");

    public final NumberPath<Long> hostId = createNumber("hostId", Long.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isVisible = createBoolean("isVisible");

    public final QLocation location;

    public final StringPath mainImageUrlOrNull = createString("mainImageUrlOrNull");

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAccommodation(String variable) {
        this(Accommodation.class, forVariable(variable), INITS);
    }

    public QAccommodation(Path<? extends Accommodation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAccommodation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAccommodation(PathMetadata metadata, PathInits inits) {
        this(Accommodation.class, metadata, inits);
    }

    public QAccommodation(Class<? extends Accommodation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.location = inits.isInitialized("location") ? new QLocation(forProperty("location")) : null;
    }

}

