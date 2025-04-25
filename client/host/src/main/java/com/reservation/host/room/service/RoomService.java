package com.reservation.host.room.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.room.Room;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.room.repository.JpaRoomRepository;
import com.reservation.host.room.repository.RoomQueryRepository;
import com.reservation.host.room.service.dto.DefaultRoomInfo;
import com.reservation.host.room.service.dto.SearchRoomResult;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {
	private final JpaRoomRepository jpaRoomRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final RoomQueryRepository roomQueryRepository;

	@Transactional
	public long create(DefaultRoomInfo createRoomInfo, long hostId) {
		checkAccommodation(createRoomInfo.accommodationId(), hostId);

		if (jpaRoomRepository.existsByNameAndAccommodationId(createRoomInfo.name(), createRoomInfo.accommodationId())) {
			throw ErrorCode.CONFLICT.exception("이미 존재하는 룸 이름 입니다.");
		}

		Room newRoom = Room.builder()
			.price(createRoomInfo.price())
			.capacity(createRoomInfo.capacity())
			.descriptionOrNull(createRoomInfo.descriptionOrNull())
			.roomCount(createRoomInfo.roomCount())
			.name(createRoomInfo.name())
			.accommodationId(createRoomInfo.accommodationId())
			.build();

		return jpaRoomRepository.save(newRoom).getId();
	}

	private void checkAccommodation(long accommodationId, long hostId) {
		if (!jpaAccommodationRepository.existsByIdAndHostId(accommodationId, hostId)) {
			throw ErrorCode.NOT_FOUND.exception("숙소를 찾을 수 없습니다.");
		}
	}

	@Transactional
	public long update(long roomId, DefaultRoomInfo updateRoomInfo, long hostId) {
		checkAccommodation(updateRoomInfo.accommodationId(), hostId);

		if (!jpaRoomRepository.existsByIdAndAccommodationId(roomId, updateRoomInfo.accommodationId())) {
			throw ErrorCode.NOT_FOUND.exception("룸 정보를 찾을 수 없습니다.");
		}

		Optional<Room> existedRoom =
			jpaRoomRepository.findOneByNameAndAccommodationId(updateRoomInfo.name(), updateRoomInfo.accommodationId());

		if (existedRoom.isPresent() && roomId != existedRoom.get().getId()) {
			throw ErrorCode.CONFLICT.exception("이미 존재하는 룸 이름 입니다.");
		}

		Room updateRoom = Room.builder()
			.id(roomId)
			.price(updateRoomInfo.price())
			.capacity(updateRoomInfo.capacity())
			.descriptionOrNull(updateRoomInfo.descriptionOrNull())
			.name(updateRoomInfo.name())
			.roomCount(updateRoomInfo.roomCount())
			.accommodationId(updateRoomInfo.accommodationId())
			.build();

		return jpaRoomRepository.save(updateRoom).getId();
	}

	public Page<SearchRoomResult> search(long hostId, String roomNameOrNull, PageRequest pageRequest) {
		long accommodationId = checkAccommodationByHostId(hostId).getId();
		return roomQueryRepository.pagingByAccommodationIdAndNameOrNull(accommodationId, roomNameOrNull, pageRequest);
	}

	private Accommodation checkAccommodationByHostId(long hostId) {
		return jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("업체 숙소 정보가 존재하지 않습니다."));
	}

	public Room findOne(long roomId, long hostId) {
		long accommodationId = checkAccommodationByHostId(hostId).getId();

		return jpaRoomRepository.findOneByIdAndAccommodationId(roomId, accommodationId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("조회하려는 룸 타입을 찾을 수 없습니다."));
	}
}
