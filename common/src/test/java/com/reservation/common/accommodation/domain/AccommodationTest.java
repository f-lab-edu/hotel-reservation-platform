package com.reservation.common.accommodation.domain;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.reservation.common.host.domain.Host;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.host.HostStatus;

class AccommodationTest {

	@Test
	@DisplayName("Accommodation 생성 성공")
	void createAccommodation_Success() {
		Host host = new Host(1L, "12341234", "kcm@mail.com", HostStatus.ACTIVE);
		Location location = new Location("Test Address", 37.7749, 127.4194);

		Accommodation accommodation = new Accommodation.AccommodationBuilder()
			.id(1L)
			.host(host)
			.name("Test Accommodation")
			.descriptionOrNull("Test Description")
			.location(location)
			.isVisible(true)
			.mainImageUrlOrNull("http://example.com/image.jpg")
			.contactNumber("010-1234-5678")
			.build();

		assertThat(accommodation).isNotNull();
		assertThat(accommodation.getName()).isEqualTo("Test Accommodation");
		assertThat(accommodation.getLocation().getAddress()).isEqualTo("Test Address");
		assertThat(accommodation.getContactNumber()).isEqualTo("010-1234-5678");
	}

	@Test
	@DisplayName("숙소 ID가 0 이하일 때 예외 발생")
	void createAccommodation_ThrowsExceptionWhenIdIsInvalid() {
		Host host = new Host(1L, "12341234", "kcm@mail.com", HostStatus.ACTIVE);
		Location location = new Location("Test Address", 37.7749, 127.4194);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Accommodation.AccommodationBuilder()
				.id(0L)
				.host(host)
				.name("Test Accommodation")
				.location(location)
				.contactNumber("010-1234-5678")
				.build();
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 ID는 0보다 커야 합니다.");
	}

	@Test
	@DisplayName("호스트가 null일 때 예외 발생")
	void createAccommodation_ThrowsExceptionWhenHostIsNull() {
		Location location = new Location("Test Address", 37.7749, 127.4194);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Accommodation.AccommodationBuilder()
				.id(1L)
				.host(null)
				.name("Test Accommodation")
				.location(location)
				.contactNumber("010-1234-5678")
				.build();
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 소유자는 필수 입니다.");
	}

	@Test
	@DisplayName("숙소 이름이 null 또는 빈 값일 때 예외 발생")
	void createAccommodation_ThrowsExceptionWhenNameIsInvalid() {
		Host host = new Host(1L, "12341234", "kcm@mail.com", HostStatus.ACTIVE);
		Location location = new Location("Test Address", 37.7749, 127.4194);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Accommodation.AccommodationBuilder()
				.id(1L)
				.host(host)
				.name("")
				.location(location)
				.contactNumber("010-1234-5678")
				.build();
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 이름은 필수 입니다.");
	}

	@Test
	@DisplayName("숙소 위치가 null일 때 예외 발생")
	void createAccommodation_ThrowsExceptionWhenLocationIsNull() {
		Host host = new Host(1L, "12341234", "kcm@mail.com", HostStatus.ACTIVE);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Accommodation.AccommodationBuilder()
				.id(1L)
				.host(host)
				.name("Test Accommodation")
				.location(null)
				.contactNumber("010-1234-5678")
				.build();
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 위치는 필수 입니다.");
	}

	@Test
	@DisplayName("숙소 연락처가 null 또는 빈 값일 때 예외 발생")
	void createAccommodation_ThrowsExceptionWhenContactNumberIsInvalid() {
		Host host = new Host(1L, "12341234", "kcm@mail.com", HostStatus.ACTIVE);
		Location location = new Location("Test Address", 37.7749, 127.4194);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Accommodation.AccommodationBuilder()
				.id(1L)
				.host(host)
				.name("Test Accommodation")
				.location(location)
				.contactNumber("")
				.build();
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 연락처는 필수 입니다.");
	}

	@Test
	@DisplayName("숙소 대표 이미지 URL이 잘못된 형식일 때 예외 발생")
	void createAccommodation_ThrowsExceptionWhenMainImageUrlIsInvalid() {
		Host host = new Host(1L, "12341234", "kcm@mail.com", HostStatus.ACTIVE);
		Location location = new Location("Test Address", 37.7749, 127.4194);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Accommodation.AccommodationBuilder()
				.id(1L)
				.host(host)
				.name("Test Accommodation")
				.location(location)
				.mainImageUrlOrNull("invalid-url")
				.contactNumber("010-1234-5678")
				.build();
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 대표 이미지 URL은 http 또는 https로 시작해야 합니다.");
	}
}
