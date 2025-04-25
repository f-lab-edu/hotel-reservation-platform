package com.reservation.host.roomimage.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.room.Room;
import com.reservation.domain.roomimage.RoomImage;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.room.repository.JpaRoomRepository;
import com.reservation.host.roomimage.repository.JpaRoomImageRepository;
import com.reservation.host.roomimage.service.dto.DefaultRoomImageInfo;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomImageService {
	private final JpaRoomImageRepository jpaRoomImageRepository;
	private final JpaRoomRepository jpaRoomRepository;
	private final JpaAccommodationRepository jpaAccommodationRepository;

	@Transactional
	public void updateRoomImagesRequest(long roomId, List<DefaultRoomImageInfo> updateRoomImageInfos, Long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("숙소를 정보가 존재하지 않습니다."));

		Room findRoom = jpaRoomRepository.findOneByIdAndAccommodationId(roomId, findAccommodation.getId())
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당하는 방을 찾을 수 없습니다."));

		// 기존 객실타입 이미지
		List<RoomImage> existingRoomImages = jpaRoomImageRepository.findByRoomTypeId(roomId);

		// 객실타입 이미지 업데이트
		List<RoomImage> newRoomImages = updateRoomImages(updateRoomImageInfos, existingRoomImages);

		List<Long> newRoomImageIds = newRoomImages.stream().map(RoomImage::getId).toList();

		// 불필요해진 기존 객실 이미지 삭제
		deleteRoomImages(existingRoomImages, newRoomImageIds);
	}

	private List<RoomImage> updateRoomImages(
		List<DefaultRoomImageInfo> updateRoomImageInfos,
		List<RoomImage> existingRoomImages) {

		List<Long> existingRoomImageIds = existingRoomImages.stream()
			.map(RoomImage::getId)
			.toList();

		// 업데이트할 객실타입 이미지
		List<RoomImage> updateRoomImages = new ArrayList<>();

		for (DefaultRoomImageInfo updateRoomImageInfo : updateRoomImageInfos) {
			if (updateRoomImageInfo.id() != null && !existingRoomImageIds.contains(updateRoomImageInfo.id())) {
				throw ErrorCode.NOT_FOUND.exception("해당하는 객실타입 이미지를 찾을 수 없습니다.");
			}
			// 기존 이미지를 순서 변경 또는 메인 이미지로 업데이트하는 경우에 한정됨 (이미지 URL 바뀌면 새로운 이미지 등록과 같다)
			if (updateRoomImageInfo.id() != null && existingRoomImageIds.contains(updateRoomImageInfo.id())) {
				RoomImage existedRoomImage = existingRoomImages.stream()
					.filter(existingRoomImage -> existingRoomImage.getId().equals(updateRoomImageInfo.id()))
					.findFirst()
					.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당하는 객실타입 이미지를 찾을 수 없습니다."));

				existedRoomImage.updateDisplayOrderAndIsMainImage(
					updateRoomImageInfo.displayOrder(),
					updateRoomImageInfo.isMainImage());

				updateRoomImages.add(existedRoomImage);
			}
		}
		// 기존 이미지 업데이트
		return jpaRoomImageRepository.saveAll(updateRoomImages);
	}

	private void deleteRoomImages(List<RoomImage> existingRoomImages, List<Long> newRoomImageIds) {
		List<Long> existingRoomImageIds = existingRoomImages.stream()
			.map(RoomImage::getId)
			.toList();

		List<Long> deletedRoomImageIds = existingRoomImageIds.stream()
			.filter(existingRoomImageId -> !newRoomImageIds.contains(existingRoomImageId))
			.toList();

		jpaRoomImageRepository.deleteAllById(deletedRoomImageIds);
	}

	public List<RoomImage> findRoomImages(Long roomTypeId, Long hostId) {
		Accommodation findAccommodation = jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("숙소를 정보가 존재하지 않습니다."));

		Room findRoom = jpaRoomRepository.findOneByIdAndAccommodationId(roomTypeId, findAccommodation.getId())
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당하는 객실타입을 찾을 수 없습니다."));

		return jpaRoomImageRepository.findByRoomTypeId(findRoom.getId());
	}
}
