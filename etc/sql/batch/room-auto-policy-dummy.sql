# 외래키 제약 사항 false
SET foreign_key_checks = 0; 

# 테이블 초기화를 위한 Drop
DROP TABLE hotel.room_auto_availability_policy ;

# 테이블 생성
CREATE TABLE hotel.`room_auto_availability_policy` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `enabled` bit(1) NOT NULL,
    `max_rooms_per_day_or_null` int DEFAULT NULL,
    `open_days_ahead_or_null` int DEFAULT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`)
)

# 외래키 제약 사항 true
SET foreign_key_checks = 1;


# 프로시저 등록
DELIMITER $$
CREATE PROCEDURE dummyAutoPolicy()
BEGIN
	DECLARE i INT DEFAULT 1;
	WHILE (i <= 20000)
		DO
			INSERT INTO hotel.room_auto_availability_policy(id, created_at, updated_at, enabled, max_rooms_per_day_or_null, open_days_ahead_or_null, room_id) 
							VALUES (i, NOW(6), NOW(6), true, 5, 90, i);
        SET i = i + 1;
END WHILE;
END $$
DELIMITER ;

# 프로시저 확인
SHOW CREATE PROCEDURE dummyAutoPolicy;

# 프로시저 삭제
DROP PROCEDURE dummyAutoPolicy;

# 프로시저 호출
CALL dummyAutoPolicy();

# 더미 데이터 rows 생성 확인
SELECT COUNT(*) FROM hotel.room_auto_availability_policy;
