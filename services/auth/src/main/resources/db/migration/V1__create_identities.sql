CREATE TABLE identities
(
    id                  BIGINT UNSIGNED                         NOT NULL,
    email               VARCHAR(254) COLLATE utf8mb4_0900_ai_ci NOT NULL,
    password_hash       VARCHAR(255)                            NOT NULL,
    role                VARCHAR(32)                             NOT NULL COMMENT '"MEMBER", "HOST", "ADMIN"',
    status              VARCHAR(16)                             NOT NULL DEFAULT 'PENDING' COMMENT '"PENDING", "ACTIVE", "LOCKED", "DISABLED"',
    email_verified_at   DATETIME(3)                             NULL,
    failed_login_count  INT UNSIGNED                            NOT NULL DEFAULT 0,
    locked_until        DATETIME(3)                             NULL,
    password_updated_at DATETIME(3)                             NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    deleted_at          DATETIME(3)                             NULL,
    is_active           TINYINT(1) AS (IF(deleted_at IS NULL, 1, 0)) STORED,
    created_at          DATETIME(3)                             NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at          DATETIME(3)                             NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),

    PRIMARY KEY (id),
    UNIQUE KEY uq_identities_email_active (email, is_active),
    KEY ix_identities_status (status),
    KEY ix_identities_created_at (created_at),

    CONSTRAINT ck_identities_status CHECK (status IN ('PENDING', 'ACTIVE', 'LOCKED', 'DISABLED')),
    CONSTRAINT ck_identities_role CHECK (role IN ('MEMBER', 'HOST', 'ADMIN'))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
