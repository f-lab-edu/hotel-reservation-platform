package com.reservation.common.accommodation.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.reservation.common.accommodation.domain.RoomImage;
import com.reservation.common.accommodation.repository.mapper.RoomImageMapper;
import com.reservation.commonapi.host.repository.HostRoomImageRepository;
import com.reservation.commonmodel.accommodation.RoomImageDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomImageRepository implements HostRoomImageRepository {
	private final JpaRoomImageRepository jpaRoomImageRepository;

	@Override
	public List<RoomImageDto> findByRoomTypeId(Long roomTypeId) {
		return jpaRoomImageRepository.findByRoomTypeId(roomTypeId)
			.stream()
			.map(RoomImageMapper::fromEntityToDto)
			.toList();
	}

	@Override
	public void deleteAllById(List<Long> deletedRoomImageIds) {
		jpaRoomImageRepository.deleteAllById(deletedRoomImageIds);
	}

	@Override
	public void saveAll(List<RoomImageDto> newRoomImages) {
		List<RoomImage> roomImages = newRoomImages.stream()
			.map(RoomImageMapper::fromDtoToEntity)
			.toList();
		jpaRoomImageRepository.saveAll(roomImages);
	}
}
