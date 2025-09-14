CREATE TABLE `accommodation`
(
    `id`            BIGINT       NOT NULL,
    `hostId`        BIGINT       NOT NULL COMMENT '호스트 업체 고유 ID',
    `name`          VARCHAR(255) NOT NULL COMMENT '숙소 이름',
    `description`   TEXT         NOT NULL COMMENT '숙소 설명',
    `contactNumber` VARCHAR(20)  NOT NULL COMMENT '연락처',
    `city`          VARCHAR(20)  NOT NULL COMMENT '도시',
    `address`       VARCHAR(255) NOT NULL COMMENT '전체 주소지',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_accommodation_hostId` (`hostId`), -- host 당 1개의 숙소 정보만 존재
    INDEX `accommodation_city_key` (`city`)            -- city 기준 조회가 많으므로 INDEX 추가
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
