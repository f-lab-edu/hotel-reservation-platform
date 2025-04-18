package com.reservation.host.accommodation.service;

import static com.reservation.host.accommodation.service.mapper.RoomTypeDtoMapper.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.reservation.common.accommodation.repository.RoomImageRepository;
import com.reservation.commonapi.host.query.HostRoomTypeQueryCondition;
import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonapi.host.repository.dto.HostRoomTypeDto;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.host.accommodation.controller.dto.request.CreateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.request.RoomTypeSearchCondition;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.response.FindOneRoomTypeResponse;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomTypeService {
	private final HostRoomTypeRepository roomTypeRepository;
	private final HostAccommodationRepository accommodationRepository;
	private final RoomImageRepository roomImageRepository;

	@Transactional
	public Long createRoomType(@Valid CreateRoomTypeRequest request, Long hostId) {
		checkAccommodation(request.accommodationId(), hostId);
		if (roomTypeRepository.existsByNameAndAccommodationId(request.name(), request.accommodationId())) {
			throw ErrorCode.CONFLICT.exception("이미 존재하는 룸 타입입니다.");
		}

		RoomTypeDto roomTypeDto = fromCreateRequest(request);
		return roomTypeRepository.save(roomTypeDto).id();
	}

	private void checkAccommodation(Long accommodationId, Long hostId) {
		if (!accommodationRepository.existsByIdAndHostId(accommodationId, hostId)) {
			throw ErrorCode.NOT_FOUND.exception("숙소를 찾을 수 없습니다.");
		}
	}

	@Transactional
	public Long updateRoomType(@Valid UpdateRoomTypeRequest request, Long hostId) {
		checkAccommodation(request.accommodationId(), hostId);

		if (!roomTypeRepository.existsByIdAndAccommodationId(request.id(), request.accommodationId())) {
			throw ErrorCode.NOT_FOUND.exception("룸타입을 찾을 수 없습니다.");
		}

		Optional<RoomTypeDto> findRoomType = roomTypeRepository.findOneByNameAndAccommodationId(request.name(),
			request.accommodationId());
		if (findRoomType.isPresent() && !findRoomType.get().id().equals(request.id())) {
			throw ErrorCode.CONFLICT.exception("이미 존재하는 룸 타입입니다.");
		}

		RoomTypeDto roomTypeDto = fromUpdateRequest(request);
		return roomTypeRepository.save(roomTypeDto).id();
	}

	public Page<HostRoomTypeDto> findRoomTypes(Long hostId, RoomTypeSearchCondition condition) {
		Long accommodationId = checkAccommodationByHostId(hostId).id();
		HostRoomTypeQueryCondition queryCondition = fromSearchConditionToQueryCondition(condition);
		return roomTypeRepository.findRoomTypes(accommodationId, queryCondition);
	}

	private AccommodationDto checkAccommodationByHostId(Long hostId) {
		return accommodationRepository.findByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("업체 숙소 정보가 존재하지 않습니다."));
	}

	public FindOneRoomTypeResponse findOneRoomType(Long hostId, Long id) {
		Long accommodationId = checkAccommodationByHostId(hostId).id();

		RoomTypeDto roomTypeDto = roomTypeRepository.findOneByIdAndAccommodationId(id, accommodationId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("조회하려는 룸 타입을 찾을 수 없습니다."));

		List<RoomImageDto> roomImages = roomImageRepository.findByRoomTypeId(roomTypeDto.id());

		return new FindOneRoomTypeResponse(roomTypeDto, roomImages);
	}
}
