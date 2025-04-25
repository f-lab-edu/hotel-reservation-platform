package com.reservation.domain.terms;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QClause is a Querydsl query type for Clause
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QClause extends EntityPathBase<Clause> {

    private static final long serialVersionUID = -629786831L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QClause clause = new QClause("clause");

    public final com.reservation.domain.base.QBaseEntity _super = new com.reservation.domain.base.QBaseEntity(this);

    public final NumberPath<Integer> clauseOrder = createNumber("clauseOrder", Integer.class);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QTerms terms;

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QClause(String variable) {
        this(Clause.class, forVariable(variable), INITS);
    }

    public QClause(Path<? extends Clause> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QClause(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QClause(PathMetadata metadata, PathInits inits) {
        this(Clause.class, metadata, inits);
    }

    public QClause(Class<? extends Clause> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.terms = inits.isInitialized("terms") ? new QTerms(forProperty("terms")) : null;
    }

}

