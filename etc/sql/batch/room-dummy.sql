# 외래키 제약 사항 false
SET foreign_key_checks = 0; 

# 테이블 초기화를 위한 Drop
DROP TABLE hotel.roomType;

# 테이블 생성
CREATE TABLE hotel.`roomType` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `accommodation_id` bigint NOT NULL,
    `capacity` int NOT NULL,
    `description_or_null` varchar(255) DEFAULT NULL,
    `name` varchar(255) NOT NULL,
    `price` int NOT NULL,
    `room_count` int NOT NULL,
    PRIMARY KEY (`id`)
)

# 외래키 제약 사항 true
SET foreign_key_checks = 1;

# 프로시저 등록
DELIMITER $$
CREATE PROCEDURE dummyRoom()
BEGIN
	DECLARE i INT DEFAULT 1;
	DECLARE j INT DEFAULT 1;
	WHILE (i <= 1000)
		DO
			WHILE (j <= i * 20)
				DO
					INSERT INTO hotel.roomType(id, created_at, updated_at, accommodation_id, capacity, description_or_null, name, price, room_count)
								VALUES (j, NOW(6), NOW(6), i, 4, '더미 룸 정보', CONCAT('숙소', i, '의 Room', j), 100000, 5);
				SET j = j + 1;
END WHILE;
        SET i = i + 1;
END WHILE;
END $$
DELIMITER ;

# 프로시저 확인
SHOW CREATE PROCEDURE dummyRoom;

# 프로시저 삭제
DROP PROCEDURE dummyRoom;

# 프로시저 호출
CALL dummyRoom();

# 더미 데이터 rows 생성 확인
SELECT COUNT(*) FROM hotel.roomType;
