package com.reservation.common.member.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMemberTerms is a Querydsl query type for MemberTerms
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMemberTerms extends EntityPathBase<MemberTerms> {

    private static final long serialVersionUID = 1691688835L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMemberTerms memberTerms = new QMemberTerms("memberTerms");

    public final com.reservation.common.domain.QBaseEntity _super = new com.reservation.common.domain.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> agreedAt = createDateTime("agreedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isAgreed = createBoolean("isAgreed");

    public final QMember member;

    public final com.reservation.common.terms.domain.QTerms terms;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMemberTerms(String variable) {
        this(MemberTerms.class, forVariable(variable), INITS);
    }

    public QMemberTerms(Path<? extends MemberTerms> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMemberTerms(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMemberTerms(PathMetadata metadata, PathInits inits) {
        this(MemberTerms.class, metadata, inits);
    }

    public QMemberTerms(Class<? extends MemberTerms> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new QMember(forProperty("member")) : null;
        this.terms = inits.isInitialized("terms") ? new com.reservation.common.terms.domain.QTerms(forProperty("terms")) : null;
    }

}

