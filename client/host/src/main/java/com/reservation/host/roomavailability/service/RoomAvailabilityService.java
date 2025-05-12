package com.reservation.host.roomavailability.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.roomavailability.RoomAvailability;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.roomavailability.repository.JpaRoomAvailabilityRepository;
import com.reservation.host.roomavailability.service.dto.DefaultRoomAvailabilityInfo;
import com.reservation.host.roomtype.repository.JpaRoomTypeRepository;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
	private final JpaRoomAvailabilityRepository jpaRoomAvailabilityRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final JpaRoomTypeRepository jpaRoomTypeRepository;

	@Transactional
	public long createRoomAvailability(
		DefaultRoomAvailabilityInfo createRoomAvailabilityInfo,
		long hostId
	) {
		checkRoomType(createRoomAvailabilityInfo.roomTypeId(), hostId);

		RoomAvailability newRoomAvailability = RoomAvailability.builder()
			.roomTypeId(createRoomAvailabilityInfo.roomTypeId())
			.date(createRoomAvailabilityInfo.date())
			.price(createRoomAvailabilityInfo.price())
			.availableCount(createRoomAvailabilityInfo.availableCount())
			.build();

		return jpaRoomAvailabilityRepository.save(newRoomAvailability).getId();
	}

	private void checkRoomType(long roomTypeId, long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("숙소 정보를 찾을 수 없습니다."));

		if (!jpaRoomTypeRepository.existsByIdAndAccommodationId(roomTypeId, findAccommodation.getId())) {
			throw ErrorCode.NOT_FOUND.exception("룸타입 정보를 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long updateRoomAvailability(
		DefaultRoomAvailabilityInfo updateRoomAvailabilityInfo,
		long updateRoomAvailabilityId,
		long hostId
	) {
		checkRoomType(updateRoomAvailabilityInfo.roomTypeId(), hostId);

		RoomAvailability updateRoomAvailability = RoomAvailability.builder()
			.id(updateRoomAvailabilityId)
			.roomTypeId(updateRoomAvailabilityInfo.roomTypeId())
			.date(updateRoomAvailabilityInfo.date())
			.price(updateRoomAvailabilityInfo.price())
			.availableCount(updateRoomAvailabilityInfo.availableCount())
			.build();

		return jpaRoomAvailabilityRepository.save(updateRoomAvailability).getId();
	}

	public List<RoomAvailability> findRoomAvailability(
		Long roomTypeId,
		Long hostId,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate == null || endDate == null) {
			throw ErrorCode.BAD_REQUEST.exception("시작일과 종료일을 입력해주세요.");
		}
		if (startDate.isAfter(endDate)) {
			throw ErrorCode.BAD_REQUEST.exception("시작일은 종료일보다 이전이어야 합니다.");
		}
		if (endDate.isAfter(startDate.plusDays(90))) {
			throw ErrorCode.BAD_REQUEST.exception("최대 조회 기간은 90일입니다.");
		}

		checkRoomType(roomTypeId, hostId);

		return jpaRoomAvailabilityRepository.findByRoomTypeIdAndDateBetween(roomTypeId, startDate, endDate);
	}
}
