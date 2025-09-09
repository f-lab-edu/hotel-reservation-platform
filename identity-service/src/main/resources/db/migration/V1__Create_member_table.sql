CREATE TABLE `member`
(
    `id`                BIGINT                     NOT NULL,
    `email`             VARCHAR(255)               NOT NULL COMMENT '로그인 이메일',
    `password`          VARCHAR(255)               NOT NULL COMMENT '해싱된 비밀번호',
    `phone_number`      VARCHAR(20)                NULL COMMENT '휴대폰 번호',
    `status`            ENUM ('ACTIVE', 'DELETED') NOT NULL DEFAULT 'ACTIVE',
    `tokens_valid_from` DATETIME                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at`        DATETIME                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME                   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_member_email` (`email`) -- email을 이용한 조회가 많으므로 UNIQUE INDEX 추가
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
