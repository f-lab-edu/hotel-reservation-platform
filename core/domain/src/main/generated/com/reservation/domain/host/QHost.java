package com.reservation.domain.host;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QHost is a Querydsl query type for Host
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHost extends EntityPathBase<Host> {

    private static final long serialVersionUID = -63580973L;

    public static final QHost host = new QHost("host");

    public final com.reservation.domain.base.QBaseEntity _super = new com.reservation.domain.base.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final StringPath password = createString("password");

    public final EnumPath<com.reservation.domain.host.enums.HostStatus> status = createEnum("status", com.reservation.domain.host.enums.HostStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QHost(String variable) {
        super(Host.class, forVariable(variable));
    }

    public QHost(Path<? extends Host> path) {
        super(path.getType(), path.getMetadata());
    }

    public QHost(PathMetadata metadata) {
        super(Host.class, metadata);
    }

}

