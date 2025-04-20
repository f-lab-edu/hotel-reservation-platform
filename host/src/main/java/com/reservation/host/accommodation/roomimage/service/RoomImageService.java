package com.reservation.host.accommodation.roomimage.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostRoomImageRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.host.accommodation.roomimage.controller.dto.request.UpdateRoomImagesRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomImageService {
	private final HostRoomImageRepository roomImageRepository;
	private final HostRoomTypeRepository roomTypeRepository;
	private final HostAccommodationRepository accommodationRepository;

	@Transactional
	public void updateRoomImagesRequest(UpdateRoomImagesRequest request, Long hostId) {
		AccommodationDto accommodation = accommodationRepository.findByHostId(hostId).orElseThrow(() ->
			ErrorCode.NOT_FOUND.exception("숙소를 정보가 존재하지 않습니다.")
		);

		RoomTypeDto roomType = roomTypeRepository.findOneByIdAndAccommodationId(request.roomTypeId(),
			accommodation.id()).orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당하는 객실타입을 찾을 수 없습니다."));

		// 기존 객실타입 이미지
		List<RoomImageDto> existingRoomImages = roomImageRepository.findByRoomTypeId(request.roomTypeId());

		// 객실타입 이미지 업데이트
		List<RoomImageDto> newRoomImages = updateRoomImages(request, existingRoomImages);

		List<Long> newRoomImageIds = newRoomImages.stream()
			.map(RoomImageDto::id)
			.toList();

		// 불필요해진 기존 객실 이미지 삭제
		deleteRoomImages(existingRoomImages, newRoomImageIds);
	}

	private List<RoomImageDto> updateRoomImages(UpdateRoomImagesRequest request,
		List<RoomImageDto> existingRoomImages) {
		Long roomTypeId = request.roomTypeId();
		List<Long> existingRoomImageIds = existingRoomImages.stream()
			.map(RoomImageDto::id)
			.toList();

		// 업데이트할 객실타입 이미지
		List<RoomImageDto> updateRoomImages = new ArrayList<>();
		for (UpdateRoomImagesRequest.UpdateRoomImage updateRoomImage : request.roomImages()) {
			if (updateRoomImage.id() != null && !existingRoomImageIds.contains(updateRoomImage.id())) {
				throw ErrorCode.NOT_FOUND.exception("해당하는 객실타입 이미지를 찾을 수 없습니다.");
			}
			// 기존 이미지를 순서 변경 또는 메인 이미지로 업데이트하는 경우
			if (updateRoomImage.id() != null && existingRoomImageIds.contains(updateRoomImage.id())) {
				updateRoomImages.add(new RoomImageDto(
					updateRoomImage.id(),
					roomTypeId,
					existingRoomImages.stream().findFirst()
						.filter(existingRoomImage -> existingRoomImage.id().equals(updateRoomImage.id()))
						.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당하는 객실타입 이미지를 찾을 수 없습니다."))
						.imageUrl(),
					updateRoomImage.displayOrder(),
					updateRoomImage.isMainImage()));
				continue;
			}
		}
		// 요청 이미지 업데이트
		return roomImageRepository.saveAll(updateRoomImages);
	}

	private void deleteRoomImages(List<RoomImageDto> existingRoomImages, List<Long> newRoomImageIds) {
		List<Long> existingRoomImageIds = existingRoomImages.stream()
			.map(RoomImageDto::id)
			.toList();

		List<Long> deletedRoomImageIds = existingRoomImageIds.stream()
			.filter(existingRoomImageId -> !newRoomImageIds.contains(existingRoomImageId))
			.toList();

		roomImageRepository.deleteAllById(deletedRoomImageIds);
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
