package com.reservation.common.host.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHost is a Querydsl query type for Host
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHost extends EntityPathBase<Host> {

    private static final long serialVersionUID = -741406464L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHost host = new QHost("host");

    public final com.reservation.common.domain.QBaseEntity _super = new com.reservation.common.domain.QBaseEntity(this);

    public final com.reservation.common.accommodation.domain.QAccommodation accommodation;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath password = createString("password");

    public final EnumPath<com.reservation.commonmodel.host.HostStatus> status = createEnum("status", com.reservation.commonmodel.host.HostStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QHost(String variable) {
        this(Host.class, forVariable(variable), INITS);
    }

    public QHost(Path<? extends Host> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHost(PathMetadata metadata, PathInits inits) {
        this(Host.class, metadata, inits);
    }

    public QHost(Class<? extends Host> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.accommodation = inits.isInitialized("accommodation") ? new com.reservation.common.accommodation.domain.QAccommodation(forProperty("accommodation"), inits.get("accommodation")) : null;
    }

}

