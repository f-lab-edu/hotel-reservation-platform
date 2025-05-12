package com.reservation.host.roompricingpolicy.service;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.roompricingpolicy.repository.JpaRoomPricingPolicyRepository;
import com.reservation.host.roompricingpolicy.service.dto.DefaultRoomPricingPolicyInfo;
import com.reservation.host.roomtype.repository.JpaRoomTypeRepository;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomPricingPolicyService {
	private final JpaRoomPricingPolicyRepository jpaRoomPricingPolicyRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final JpaRoomTypeRepository jpaRoomTypeRepository;

	@Transactional
	public long create(
		long roomTypeId,
		DefaultRoomPricingPolicyInfo createDefaultRoomPricingPolicyInfo,
		long hostId
	) {
		checkRoomInfo(roomTypeId, hostId);

		RoomPricingPolicy createRoomPricingPolicy = RoomPricingPolicy.builder()
			.roomTypeId(roomTypeId)
			.dayOfWeek(createDefaultRoomPricingPolicyInfo.dayOfWeek())
			.price(createDefaultRoomPricingPolicyInfo.price())
			.build();

		return jpaRoomPricingPolicyRepository.save(createRoomPricingPolicy).getId();
	}

	private void checkRoomInfo(long roomTypeId, long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("숙소를 찾을 수 없습니다."));

		if (!jpaRoomTypeRepository.existsByIdAndAccommodationId(roomTypeId, findAccommodation.getId())) {
			throw ErrorCode.NOT_FOUND.exception("룸을 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long update(
		long roomTypeId,
		long roomPricingPolicyId,
		DefaultRoomPricingPolicyInfo updateRoomPricingPolicyInfo,
		long hostId
	) {
		RoomPricingPolicy existedRoomPricingPolicy = findOne(roomTypeId, roomPricingPolicyId, hostId);

		existedRoomPricingPolicy.update(updateRoomPricingPolicyInfo.dayOfWeek(), updateRoomPricingPolicyInfo.price());

		return jpaRoomPricingPolicyRepository.save(existedRoomPricingPolicy).getId();
	}

	@Transactional
	public void delete(long roomTypeId, long roomPricingPolicyId, long hostId) {
		RoomPricingPolicy existedRoomPricingPolicy = findOne(roomTypeId, roomPricingPolicyId, hostId);

		jpaRoomPricingPolicyRepository.delete(existedRoomPricingPolicy);
	}

	public RoomPricingPolicy findOne(long roomTypeId, long roomPricingPolicyId, long hostId) {
		checkRoomInfo(roomTypeId, hostId);

		return jpaRoomPricingPolicyRepository.findOneByIdAndRoomTypeId(roomPricingPolicyId, roomTypeId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("룸 가격 정책을 찾을 수 없습니다."));
	}

}
