package com.reservation.domain.terms;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QTerms is a Querydsl query type for Terms
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTerms extends EntityPathBase<Terms> {

    private static final long serialVersionUID = -558997627L;

    public static final QTerms terms = new QTerms("terms");

    public final com.reservation.domain.base.QBaseEntity _super = new com.reservation.domain.base.QBaseEntity(this);

    public final ListPath<Clause, QClause> clauses = this.<Clause, QClause>createList("clauses", Clause.class, QClause.class, PathInits.DIRECT2);

    public final EnumPath<com.reservation.domain.terms.enums.TermsCode> code = createEnum("code", com.reservation.domain.terms.enums.TermsCode.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> exposedFrom = createDateTime("exposedFrom", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> exposedToOrNull = createDateTime("exposedToOrNull", java.time.LocalDateTime.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isLatest = createBoolean("isLatest");

    public final EnumPath<com.reservation.domain.terms.enums.TermsStatus> status = createEnum("status", com.reservation.domain.terms.enums.TermsStatus.class);

    public final StringPath title = createString("title");

    public final EnumPath<com.reservation.domain.terms.enums.TermsType> type = createEnum("type", com.reservation.domain.terms.enums.TermsType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> version = createNumber("version", Integer.class);

    public QTerms(String variable) {
        super(Terms.class, forVariable(variable));
    }

    public QTerms(Path<? extends Terms> path) {
        super(path.getType(), path.getMetadata());
    }

    public QTerms(PathMetadata metadata) {
        super(Terms.class, metadata);
    }

}

