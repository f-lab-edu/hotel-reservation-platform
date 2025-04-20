package com.reservation.host.accommodation.roomimage.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostRoomImageRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.host.accommodation.roomimage.controller.dto.request.UpdateRoomImagesRequest;
import com.reservation.host.accommodation.roomimage.controller.dto.request.UpdateRoomImagesRequest.UpdateRoomImage;

class RoomImageServiceTest {

	@Mock
	private HostRoomImageRepository roomImageRepository;

	@Mock
	private HostRoomTypeRepository roomTypeRepository;

	@Mock
	private HostAccommodationRepository accommodationRepository;

	@InjectMocks
	private RoomImageService roomImageService;

	private UpdateRoomImagesRequest updateRequest;
	private AccommodationDto accommodationDto;
	private RoomTypeDto roomTypeDto;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		updateRequest = new UpdateRoomImagesRequest(
			1L,
			List.of(
				new UpdateRoomImage(1L, "https://mock-bucket.s3.fake/2025-04-20/uuid1", 1, true),
				new UpdateRoomImage(null, "https://mock-bucket.s3.fake/2025-04-20/uuid2", 2, false)
			)
		);

		accommodationDto = new AccommodationDto(1L, null, "Test Accommodation", null, null, true, null,
			"010-1234-5678");
		roomTypeDto = new RoomTypeDto(1L, 1L, "Test Room Type", 2, 100000, "Test Description", 5);
	}

	@Test
	@DisplayName("객실 이미지 업데이트 실패 - 숙소 없음")
	void updateRoomImagesRequest_ThrowsExceptionWhenAccommodationNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomImageService.updateRoomImagesRequest(updateRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소를 정보가 존재하지 않습니다.");
	}

	@Test
	@DisplayName("객실 이미지 업데이트 실패 - 객실 타입 없음")
	void updateRoomImagesRequest_ThrowsExceptionWhenRoomTypeNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.findOneByIdAndAccommodationId(1L, 1L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomImageService.updateRoomImagesRequest(updateRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("해당하는 객실타입을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("객실 이미지 조회 성공")
	void readRoomImagesRequest_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.findOneByIdAndAccommodationId(1L, 1L)).thenReturn(Optional.of(roomTypeDto));
		when(roomImageRepository.findByRoomTypeId(1L)).thenReturn(List.of(
			new RoomImageDto(1L, 1L, "http://image1.com", 1, true),
			new RoomImageDto(2L, 1L, "http://image2.com", 2, false)
		));

		List<RoomImageDto> roomImages = roomImageService.readRoomImagesRequest(1L, 1L);

		assertThat(roomImages).hasSize(2);
		assertThat(roomImages.get(0).imageUrl()).isEqualTo("http://image1.com");
	}

	@Test
	@DisplayName("객실 이미지 조회 실패 - 숙소 없음")
	void readRoomImagesRequest_ThrowsExceptionWhenAccommodationNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomImageService.readRoomImagesRequest(1L, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소를 정보가 존재하지 않습니다.");
	}

	@Test
	@DisplayName("객실 이미지 조회 실패 - 객실 타입 없음")
	void readRoomImagesRequest_ThrowsExceptionWhenRoomTypeNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.findOneByIdAndAccommodationId(1L, 1L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomImageService.readRoomImagesRequest(1L, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("해당하는 객실타입을 찾을 수 없습니다.");
	}
}
