package com.reservation.host.accommodation.availability.service;

import static com.reservation.host.accommodation.availability.service.mapper.RoomAvailabilityDtoMapper.*;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reservation.commonapi.host.query.HostRoomAvailabilityQueryCondition;
import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostRoomAvailabilityRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.host.accommodation.availability.controller.dto.request.CreateRoomAvailabilityRequest;
import com.reservation.host.accommodation.availability.controller.dto.request.RoomAvailabilitySearchCondition;
import com.reservation.host.accommodation.availability.controller.dto.request.UpdateRoomAvailabilityRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
	private final HostRoomAvailabilityRepository roomAvailabilityRepository;
	private final HostAccommodationRepository accommodationRepository;
	private final HostRoomTypeRepository roomTypeRepository;

	public Long createRoomAvailability(CreateRoomAvailabilityRequest request, Long hostId) {
		checkRoomType(request.roomTypeId(), hostId);
		RoomAvailabilityDto createRoomAvailability = fromCreateRequestToDto(request);
		return roomAvailabilityRepository.save(createRoomAvailability).id();
	}

	private void checkRoomType(Long roomTypeId, Long hostId) {
		AccommodationDto accommodationDto = accommodationRepository.findByHostId(hostId).orElseThrow(
			() -> ErrorCode.NOT_FOUND.exception("숙소 정보를 찾을 수 없습니다."));

		if (!roomTypeRepository.existsByIdAndAccommodationId(roomTypeId, accommodationDto.id())) {
			throw ErrorCode.NOT_FOUND.exception("룸타입 정보를 찾을 수 없습니다.");
		}
	}

	public Long updateRoomAvailability(UpdateRoomAvailabilityRequest request, Long hostId) {
		checkRoomType(request.roomTypeId(), hostId);

		RoomAvailabilityDto updateRoomAvailability = fromUpdateRequestToDto(request);
		return roomAvailabilityRepository.save(updateRoomAvailability).id();
	}

	public List<RoomAvailabilityDto> findRoomAvailability(RoomAvailabilitySearchCondition condition, Long hostId) {
		checkRoomType(condition.roomTypeId(), hostId);
		HostRoomAvailabilityQueryCondition queryCondition = fromSearchConditionToQueryCondition(condition);
		return roomAvailabilityRepository.findRoomAvailability(queryCondition);
	}
}
