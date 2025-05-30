# 외래키 제약 사항 false
SET foreign_key_checks = 0;

# 테이블 초기화를 위한 Drop
DROP TABLE hotel.host;

# 테이블 생성
CREATE TABLE `host` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `email` varchar(255) NOT NULL,
    `password` varchar(255) NOT NULL,
    `status` varchar(255) NOT NULL,
PRIMARY KEY (`id`)
)

# 외래키 제약 사항 true
SET foreign_key_checks = 1;

# 프로시저 등록
DELIMITER $$
CREATE PROCEDURE dummyHost()
BEGIN
	DECLARE i INT DEFAULT 1;
	WHILE (i <= 1000)
		DO
			INSERT INTO hotel.host(id, created_at, updated_at, email, password, status)
							VALUES (i, NOW(6), NOW(6), CONCAT('member', i, '@dummy.com'), 'hashed_password', 'ACTIVE');
        SET i = i + 1;
END WHILE;
END $$
DELIMITER ;

# 프로시저 확인
SHOW CREATE PROCEDURE dummyHost;

# 프로시저 삭제
DROP PROCEDURE dummyHost;

# 프로시저 호출
CALL dummyHost();

# 더미 데이터 rows 생성 확인
SELECT COUNT(*) FROM hotel.host;
