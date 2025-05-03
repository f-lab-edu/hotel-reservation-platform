package com.reservation.host.roomautoavailabilitypolicy.service;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.room.repository.JpaRoomRepository;
import com.reservation.host.roomautoavailabilitypolicy.repository.JpaRoomAutoAvailabilityPolicyRepository;
import com.reservation.host.roomautoavailabilitypolicy.service.dto.DefaultRoomAutoAvailabilityPolicyInfo;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAutoAvailabilityPolicyService {
	private final JpaRoomAutoAvailabilityPolicyRepository jpaRoomAutoAvailabilityPolicyRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final JpaRoomRepository jpaRoomRepository;

	@Transactional
	public long create(
		long roomId,
		DefaultRoomAutoAvailabilityPolicyInfo createRoomAutoAvailabilityPolicyInfo,
		long hostId
	) {
		validateHostAndRoom(roomId, hostId);

		RoomAutoAvailabilityPolicy createRoomAutoAvailabilityPolicy =
			RoomAutoAvailabilityPolicy.builder()
				.roomId(roomId)
				.maxRoomsPerDayOrNull(createRoomAutoAvailabilityPolicyInfo.maxRoomsPerDayOrNull())
				.openDaysAheadOrNull(createRoomAutoAvailabilityPolicyInfo.openDaysAheadOrNull())
				.enabled(createRoomAutoAvailabilityPolicyInfo.enabled())
				.build();

		return jpaRoomAutoAvailabilityPolicyRepository.save(createRoomAutoAvailabilityPolicy).getId();
	}

	private void validateHostAndRoom(long roomId, long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("숙소 정보를 찾을 수 없습니다."));

		if (!jpaRoomRepository.existsByIdAndAccommodationId(roomId, findAccommodation.getId())) {
			throw ErrorCode.CONFLICT.exception("룸 정보를 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long update(
		long roomId,
		long roomAutoAvailabilityPolicyId,
		DefaultRoomAutoAvailabilityPolicyInfo updateRoomAutoAvailabilityPolicyInfo,
		long hostId
	) {
		RoomAutoAvailabilityPolicy existedRoomAutoAvailabilityPolicy =
			findOne(roomId, roomAutoAvailabilityPolicyId, hostId);

		existedRoomAutoAvailabilityPolicy.update(
			updateRoomAutoAvailabilityPolicyInfo.enabled(),
			updateRoomAutoAvailabilityPolicyInfo.openDaysAheadOrNull(),
			updateRoomAutoAvailabilityPolicyInfo.maxRoomsPerDayOrNull());

		return jpaRoomAutoAvailabilityPolicyRepository.save(existedRoomAutoAvailabilityPolicy).getId();
	}

	public RoomAutoAvailabilityPolicy findOne(long roomId, long roomAutoAvailabilityPolicyId, long hostId) {
		validateHostAndRoom(roomId, hostId);

		return jpaRoomAutoAvailabilityPolicyRepository.findOneByIdAndRoomId(roomAutoAvailabilityPolicyId, roomId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 예약 가용 자동화 정책을 찾을 수 없습니다."));
	}
}
