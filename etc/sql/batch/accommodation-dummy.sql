# 외래키 제약 사항 false
SET foreign_key_checks = 0;

# 테이블 초기화를 위한 Drop
DROP TABLE hotel.accommodation;

# 테이블 생성
CREATE TABLE hotel.`accommodation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) NOT NULL,
    `updated_at` datetime(6) NOT NULL,
    `contact_number` varchar(255) NOT NULL,
    `description_or_null` varchar(255) DEFAULT NULL,
    `is_visible` bit(1) NOT NULL,
    `address` varchar(255) NOT NULL,
    `latitude` double NOT NULL,
    `longitude` double NOT NULL,
    `main_image_url_or_null` varchar(255) DEFAULT NULL,
    `name` varchar(255) NOT NULL,
    `host_id` bigint DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UKsy8lpxpp5nxonojnf9nna77yf` (`host_id`),
    CONSTRAINT `FKxlitwn68lomveiyomtuuys4a` FOREIGN KEY (`host_id`) REFERENCES `host` (`id`)
)

# 외래키 제약 사항 true
SET foreign_key_checks = 1;

# 프로시저 등록
DELIMITER $$
CREATE PROCEDURE dummyAccommodation()
BEGIN
	DECLARE i INT DEFAULT 1;
	WHILE (i <= 1000)
		DO
			INSERT INTO hotel.accommodation(id, created_at, updated_at, contact_number, description_or_null, is_visible, address, latitude, longitude, main_image_url_or_null, name, host_id)
							VALUES (i, NOW(6), NOW(6), '010-1234-1234', 'dummy-data', true, '판교', 37, 127, 'dummy-url.com', '더미 숙소 정보', i);
        SET i = i + 1;
END WHILE;
END $$
DELIMITER ;

# 프로시저 확인
SHOW CREATE PROCEDURE dummyAccommodation;

# 프로시저 삭제
DROP PROCEDURE dummyAccommodation;

# 프로시저 호출
CALL dummyAccommodation();

# 더미 데이터 rows 생성 확인
SELECT COUNT(*) FROM hotel.accommodation;
