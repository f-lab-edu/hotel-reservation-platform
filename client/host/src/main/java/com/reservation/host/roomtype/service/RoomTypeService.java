package com.reservation.host.roomtype.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.roomtype.repository.JpaRoomTypeRepository;
import com.reservation.host.roomtype.repository.RoomTypeQueryRepository;
import com.reservation.host.roomtype.service.dto.DefaultRoomTypeInfo;
import com.reservation.host.roomtype.service.dto.SearchRoomTypeResult;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
	private final JpaRoomTypeRepository jpaRoomTypeRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final RoomTypeQueryRepository roomTypeQueryRepository;

	@Transactional
	public long create(DefaultRoomTypeInfo createRoomInfo, long hostId) {
		checkAccommodation(createRoomInfo.accommodationId(), hostId);

		if (jpaRoomTypeRepository.existsByNameAndAccommodationId(createRoomInfo.name(),
			createRoomInfo.accommodationId())) {
			throw ErrorCode.CONFLICT.exception("이미 존재하는 룸 이름 입니다.");
		}

		RoomType newRoomType = RoomType.builder()
			.price(createRoomInfo.price())
			.capacity(createRoomInfo.capacity())
			.descriptionOrNull(createRoomInfo.descriptionOrNull())
			.roomCount(createRoomInfo.roomCount())
			.name(createRoomInfo.name())
			.accommodationId(createRoomInfo.accommodationId())
			.build();

		return jpaRoomTypeRepository.save(newRoomType).getId();
	}

	private void checkAccommodation(long accommodationId, long hostId) {
		if (!jpaAccommodationRepository.existsByIdAndHostId(accommodationId, hostId)) {
			throw ErrorCode.NOT_FOUND.exception("숙소를 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long update(long roomTypeId, DefaultRoomTypeInfo updateRoomInfo, long hostId) {
		checkAccommodation(updateRoomInfo.accommodationId(), hostId);

		if (!jpaRoomTypeRepository.existsByIdAndAccommodationId(roomTypeId, updateRoomInfo.accommodationId())) {
			throw ErrorCode.NOT_FOUND.exception("룸 정보를 찾을 수 없습니다.");
		}

		Optional<RoomType> existedRoom =
			jpaRoomTypeRepository.findOneByNameAndAccommodationId(updateRoomInfo.name(),
				updateRoomInfo.accommodationId());

		if (existedRoom.isPresent() && roomTypeId != existedRoom.get().getId()) {
			throw ErrorCode.CONFLICT.exception("이미 존재하는 룸 이름 입니다.");
		}

		RoomType updateRoomType = RoomType.builder()
			.id(roomTypeId)
			.price(updateRoomInfo.price())
			.capacity(updateRoomInfo.capacity())
			.descriptionOrNull(updateRoomInfo.descriptionOrNull())
			.name(updateRoomInfo.name())
			.roomCount(updateRoomInfo.roomCount())
			.accommodationId(updateRoomInfo.accommodationId())
			.build();

		return jpaRoomTypeRepository.save(updateRoomType).getId();
	}

	public Page<SearchRoomTypeResult> search(long hostId, String roomNameOrNull, PageRequest pageRequest) {
		long accommodationId = checkAccommodationByHostId(hostId).getId();

		return roomTypeQueryRepository.pagingByAccommodationIdAndNameOrNull(accommodationId, roomNameOrNull,
			pageRequest);
	}

	private Accommodation checkAccommodationByHostId(long hostId) {
		return jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("업체 숙소 정보가 존재하지 않습니다."));
	}

	public RoomType findOne(long roomTypeId, long hostId) {
		long accommodationId = checkAccommodationByHostId(hostId).getId();

		return jpaRoomTypeRepository.findOneByIdAndAccommodationId(roomTypeId, accommodationId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("조회하려는 룸 타입을 찾을 수 없습니다."));
	}
}
