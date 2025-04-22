package com.reservation.common.member.socialaccount.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSocialAccount is a Querydsl query type for SocialAccount
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSocialAccount extends EntityPathBase<SocialAccount> {

    private static final long serialVersionUID = 952792068L;

    public static final QSocialAccount socialAccount = new QSocialAccount("socialAccount");

    public final com.reservation.common.domain.QBaseEntity _super = new com.reservation.common.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final NumberPath<Long> memberId = createNumber("memberId", Long.class);

    public final EnumPath<com.reservation.commonmodel.auth.login.SocialLoginProvider> provider = createEnum("provider", com.reservation.commonmodel.auth.login.SocialLoginProvider.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSocialAccount(String variable) {
        super(SocialAccount.class, forVariable(variable));
    }

    public QSocialAccount(Path<? extends SocialAccount> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSocialAccount(PathMetadata metadata) {
        super(SocialAccount.class, metadata);
    }

}

