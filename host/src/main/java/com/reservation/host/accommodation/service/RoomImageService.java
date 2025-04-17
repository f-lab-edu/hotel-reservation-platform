package com.reservation.host.accommodation.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostModuleRepository;
import com.reservation.commonapi.host.repository.HostRoomImageRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomImagesRequest;
import com.reservation.host.accommodation.service.mapper.RoomImageDtoMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomImageService {
	private final HostRoomImageRepository roomImageRepository;
	private final HostRoomTypeRepository roomTypeRepository;
	private final HostModuleRepository hostRepository;
	private final HostAccommodationRepository accommodationRepository;

	public void updateRoomImagesRequest(UpdateRoomImagesRequest request, Long hostId) {
		AccommodationDto accommodation = accommodationRepository.findByHostId(hostId).orElseThrow(() ->
			ErrorCode.NOT_FOUND.exception("숙소를 정보가 존재하지 않습니다.")
		);

		RoomTypeDto roomType = roomTypeRepository.findOneByIdAndAccommodationId(request.roomTypeId(),
			accommodation.id()).orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당하는 객실타입을 찾을 수 없습니다."));

		List<RoomImageDto> existingRoomImages = roomImageRepository.findByRoomTypeId(roomType.id());

		List<RoomImageDto> requestRoomImages = request.roomImages()
			.stream()
			.map(requestRoomImage -> RoomImageDtoMapper.fromUpdateRoomImage(requestRoomImage, roomType.id()))
			.toList();

		List<Long> deletedRoomImageIds = existingRoomImages.stream()
			.filter(existingRoomImage -> !requestRoomImages.contains(existingRoomImage))
			.map(RoomImageDto::id)
			.toList();

		roomImageRepository.deleteAllById(deletedRoomImageIds);

		List<RoomImageDto> newRoomImages = requestRoomImages.stream()
			.filter(requestRoomImage -> !existingRoomImages.contains(requestRoomImage))
			.toList();

		roomImageRepository.saveAll(newRoomImages);
	}

	public List<RoomImageDto> readRoomImagesRequest(Long roomTypeId, Long hostId) {
		AccommodationDto accommodation = accommodationRepository.findByHostId(hostId).orElseThrow(() ->
			ErrorCode.NOT_FOUND.exception("숙소를 정보가 존재하지 않습니다.")
		);

		RoomTypeDto roomType = roomTypeRepository.findOneByIdAndAccommodationId(roomTypeId, accommodation.id())
			.orElseThrow(() ->
				ErrorCode.NOT_FOUND.exception("해당하는 객실타입을 찾을 수 없습니다.")
			);

		return roomImageRepository.findByRoomTypeId(roomType.id());
	}
}
