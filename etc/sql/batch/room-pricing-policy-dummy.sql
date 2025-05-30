# 외래키 제약 사항 false
SET foreign_key_checks = 0; 

# 테이블 초기화를 위한 Drop
DROP TABLE hotel.room_pricing_policy;

# 테이블 생성
CREATE TABLE hotel.`room_pricing_policy` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `day_of_week` enum('FRIDAY','MONDAY','SATURDAY','SUNDAY','THURSDAY','TUESDAY','WEDNESDAY') NOT NULL,
    `price` int NOT NULL,
    `room_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_room_pricing_policy` (`day_of_week`,`room_id`)
)

# 외래키 제약 사항 true
SET foreign_key_checks = 1;

# 프로시저 등록
DELIMITER $$
CREATE PROCEDURE dummyPricingPolicy()
BEGIN
	DECLARE i INT DEFAULT 1;
	WHILE (i <= 20000)
		DO
			INSERT INTO hotel.room_pricing_policy(id, created_at, updated_at, day_of_week, price, room_id) 
							VALUES ((i - 1) * 7 + 1, NOW(6), NOW(6), 'FRIDAY', 200000, i), 
								   ((i - 1) * 7 + 2, NOW(6), NOW(6), 'MONDAY', 90000, i),
								   ((i - 1) * 7 + 3, NOW(6), NOW(6), 'SATURDAY', 250000, i),
								   ((i - 1) * 7 + 4, NOW(6), NOW(6), 'SUNDAY', 150000, i),
								   ((i - 1) * 7 + 5, NOW(6), NOW(6), 'THURSDAY', 120000, i),
								   ((i - 1) * 7 + 6, NOW(6), NOW(6), 'TUESDAY', 100000, i),
								   ((i - 1) * 7 + 7, NOW(6), NOW(6), 'WEDNESDAY', 110000, i);
        SET i = i + 1;
END WHILE;
END $$
DELIMITER ;

# 프로시저 확인
SHOW CREATE PROCEDURE dummyPricingPolicy;

# 프로시저 삭제
DROP PROCEDURE dummyPricingPolicy;

# 프로시저 호출
CALL dummyPricingPolicy();

# 더미 데이터 rows 생성 확인
SELECT COUNT(*) FROM hotel.room_pricing_policy;
