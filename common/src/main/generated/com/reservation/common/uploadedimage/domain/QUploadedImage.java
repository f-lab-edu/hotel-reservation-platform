package com.reservation.common.uploadedimage.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUploadedImage is a Querydsl query type for UploadedImage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUploadedImage extends EntityPathBase<UploadedImage> {

    private static final long serialVersionUID = 688763576L;

    public static final QUploadedImage uploadedImage = new QUploadedImage("uploadedImage");

    public final com.reservation.common.domain.QBaseEntity _super = new com.reservation.common.domain.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<ImageDomain> domain = createEnum("domain", ImageDomain.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DatePath<java.time.LocalDate> uploadDate = createDate("uploadDate", java.time.LocalDate.class);

    public final NumberPath<Long> uploaderId = createNumber("uploaderId", Long.class);

    public final EnumPath<com.reservation.commonmodel.auth.Role> uploaderRole = createEnum("uploaderRole", com.reservation.commonmodel.auth.Role.class);

    public final StringPath uuid = createString("uuid");

    public QUploadedImage(String variable) {
        super(UploadedImage.class, forVariable(variable));
    }

    public QUploadedImage(Path<? extends UploadedImage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUploadedImage(PathMetadata metadata) {
        super(UploadedImage.class, metadata);
    }

}

