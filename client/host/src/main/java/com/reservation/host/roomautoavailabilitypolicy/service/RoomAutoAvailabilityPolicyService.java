package com.reservation.host.roomautoavailabilitypolicy.service;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.roomautoavailabilitypolicy.repository.JpaRoomAutoAvailabilityPolicyRepository;
import com.reservation.host.roomautoavailabilitypolicy.service.dto.DefaultRoomAutoAvailabilityPolicyInfo;
import com.reservation.host.roomtype.repository.JpaRoomTypeRepository;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAutoAvailabilityPolicyService {
	private final JpaRoomAutoAvailabilityPolicyRepository jpaRoomAutoAvailabilityPolicyRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final JpaRoomTypeRepository jpaRoomTypeRepository;

	@Transactional
	public long create(
		long roomTypeId,
		DefaultRoomAutoAvailabilityPolicyInfo createRoomAutoAvailabilityPolicyInfo,
		long hostId
	) {
		validateHostAndRoom(roomTypeId, hostId);

		RoomAutoAvailabilityPolicy createRoomAutoAvailabilityPolicy =
			RoomAutoAvailabilityPolicy.builder()
				.roomTypeId(roomTypeId)
				.maxRoomsPerDayOrNull(createRoomAutoAvailabilityPolicyInfo.maxRoomsPerDayOrNull())
				.openDaysAheadOrNull(createRoomAutoAvailabilityPolicyInfo.openDaysAheadOrNull())
				.enabled(createRoomAutoAvailabilityPolicyInfo.enabled())
				.build();

		return jpaRoomAutoAvailabilityPolicyRepository.save(createRoomAutoAvailabilityPolicy).getId();
	}

	private void validateHostAndRoom(long roomTypeId, long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("숙소 정보를 찾을 수 없습니다."));

		if (!jpaRoomTypeRepository.existsByIdAndAccommodationId(roomTypeId, findAccommodation.getId())) {
			throw ErrorCode.CONFLICT.exception("룸 정보를 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long update(
		long roomTypeId,
		long roomAutoAvailabilityPolicyId,
		DefaultRoomAutoAvailabilityPolicyInfo updateRoomAutoAvailabilityPolicyInfo,
		long hostId
	) {
		RoomAutoAvailabilityPolicy existedRoomAutoAvailabilityPolicy =
			findOne(roomTypeId, roomAutoAvailabilityPolicyId, hostId);

		existedRoomAutoAvailabilityPolicy.update(
			updateRoomAutoAvailabilityPolicyInfo.enabled(),
			updateRoomAutoAvailabilityPolicyInfo.openDaysAheadOrNull(),
			updateRoomAutoAvailabilityPolicyInfo.maxRoomsPerDayOrNull());

		return jpaRoomAutoAvailabilityPolicyRepository.save(existedRoomAutoAvailabilityPolicy).getId();
	}

	public RoomAutoAvailabilityPolicy findOne(long roomTypeId, long roomAutoAvailabilityPolicyId, long hostId) {
		validateHostAndRoom(roomTypeId, hostId);

		return jpaRoomAutoAvailabilityPolicyRepository.findOneByIdAndRoomTypeId(roomAutoAvailabilityPolicyId,
				roomTypeId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 예약 가용 자동화 정책을 찾을 수 없습니다."));
	}
}
