package com.reservation.host.roompricingpolicy.service;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.room.repository.JpaRoomRepository;
import com.reservation.host.roompricingpolicy.repository.JpaRoomPricingPolicyRepository;
import com.reservation.host.roompricingpolicy.service.dto.DefaultRoomPricingPolicyInfo;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomPricingPolicyService {
	private final JpaRoomPricingPolicyRepository jpaRoomPricingPolicyRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final JpaRoomRepository jpaRoomRepository;

	@Transactional
	public long create(
		long roomId,
		DefaultRoomPricingPolicyInfo createDefaultRoomPricingPolicyInfo,
		long hostId
	) {
		checkRoomInfo(roomId, hostId);

		RoomPricingPolicy createRoomPricingPolicy = RoomPricingPolicy.builder()
			.roomId(roomId)
			.dayOfWeek(createDefaultRoomPricingPolicyInfo.dayOfWeek())
			.price(createDefaultRoomPricingPolicyInfo.price())
			.build();

		return jpaRoomPricingPolicyRepository.save(createRoomPricingPolicy).getId();
	}

	private void checkRoomInfo(long roomId, long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("숙소를 찾을 수 없습니다."));

		if (!jpaRoomRepository.existsByIdAndAccommodationId(roomId, findAccommodation.getId())) {
			throw ErrorCode.NOT_FOUND.exception("룸을 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long update(
		long roomId,
		long roomPricingPolicyId,
		DefaultRoomPricingPolicyInfo updateRoomPricingPolicyInfo,
		long hostId
	) {
		RoomPricingPolicy existedRoomPricingPolicy = findOne(roomId, roomPricingPolicyId, hostId);

		existedRoomPricingPolicy.update(updateRoomPricingPolicyInfo.dayOfWeek(), updateRoomPricingPolicyInfo.price());

		return jpaRoomPricingPolicyRepository.save(existedRoomPricingPolicy).getId();
	}

	@Transactional
	public void delete(long roomId, long roomPricingPolicyId, long hostId) {
		RoomPricingPolicy existedRoomPricingPolicy = findOne(roomId, roomPricingPolicyId, hostId);

		jpaRoomPricingPolicyRepository.delete(existedRoomPricingPolicy);
	}

	public RoomPricingPolicy findOne(long roomId, long roomPricingPolicyId, long hostId) {
		checkRoomInfo(roomId, hostId);

		return jpaRoomPricingPolicyRepository.findOneByIdAndRoomId(roomPricingPolicyId, roomId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 가격 정책을 찾을 수 없습니다."));
	}

}
