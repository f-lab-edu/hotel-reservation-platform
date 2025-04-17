package com.reservation.host.accommodation.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.reservation.common.accommodation.repository.RoomImageRepository;
import com.reservation.commonapi.host.query.HostRoomTypeQueryCondition;
import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonapi.host.repository.dto.HostRoomTypeDto;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomImageDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.host.accommodation.controller.dto.request.CreateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.request.RoomTypeSearchCondition;
import com.reservation.host.accommodation.controller.dto.request.UpdateRoomTypeRequest;
import com.reservation.host.accommodation.controller.dto.response.FindOneRoomTypeResponse;

class RoomTypeServiceTest {

	@Mock
	private HostRoomTypeRepository roomTypeRepository;

	@Mock
	private HostAccommodationRepository accommodationRepository;

	@Mock
	private RoomImageRepository roomImageRepository;

	@InjectMocks
	private RoomTypeService roomTypeService;

	private CreateRoomTypeRequest createRequest;
	private UpdateRoomTypeRequest updateRequest;
	private AccommodationDto accommodationDto;
	private RoomTypeDto roomTypeDto;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		createRequest = new CreateRoomTypeRequest(1L, "Deluxe Room", 2, 100, "Description", 1);
		updateRequest = new UpdateRoomTypeRequest(1L, 1L, "Updated Room", 2, 150, "Updated Description", 2);

		accommodationDto = new AccommodationDto(1L, null, "Test Accommodation", null, null, true, null,
			"010-1234-5678");
		roomTypeDto = new RoomTypeDto(1L, 1L, "Deluxe Room", 2, 100, "Description", 1);
	}

	@Test
	@DisplayName("룸 타입 생성 성공")
	void createRoomType_Success() {
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(true);
		when(roomTypeRepository.existsByNameAndAccommodationId("Deluxe Room", 1L)).thenReturn(false);
		when(roomTypeRepository.save(any(RoomTypeDto.class))).thenReturn(roomTypeDto);

		Long roomTypeId = roomTypeService.createRoomType(createRequest, 1L);

		assertThat(roomTypeId).isEqualTo(1L);
		verify(roomTypeRepository).save(any(RoomTypeDto.class));
	}

	@Test
	@DisplayName("룸 타입 생성 실패 - 숙소 없음")
	void createRoomType_ThrowsExceptionWhenAccommodationNotFound() {
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomTypeService.createRoomType(createRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("룸 타입 생성 실패 - 중복된 룸 타입")
	void createRoomType_ThrowsExceptionWhenRoomTypeExists() {
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(true);
		when(roomTypeRepository.existsByNameAndAccommodationId("Deluxe Room", 1L)).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomTypeService.createRoomType(createRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("이미 존재하는 룸 타입입니다.");
	}

	@Test
	@DisplayName("룸 타입 수정 성공")
	void updateRoomType_Success() {
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(true);
		when(roomTypeRepository.existsByIdAndAccommodationId(1L, 1L)).thenReturn(true);
		when(roomTypeRepository.findOneByNameAndAccommodationId("Updated Room", 1L)).thenReturn(Optional.empty());
		when(roomTypeRepository.save(any(RoomTypeDto.class))).thenReturn(roomTypeDto);

		Long roomTypeId = roomTypeService.updateRoomType(updateRequest, 1L);

		assertThat(roomTypeId).isEqualTo(1L);
		verify(roomTypeRepository).save(any(RoomTypeDto.class));
	}

	@Test
	@DisplayName("룸 타입 수정 실패 - 룸 타입 없음")
	void updateRoomType_ThrowsExceptionWhenRoomTypeNotFound() {
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(true);
		when(roomTypeRepository.existsByIdAndAccommodationId(1L, 1L)).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomTypeService.updateRoomType(updateRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("룸타입을 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("룸 타입 목록 조회 성공")
	void findRoomTypes_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.findRoomTypes(anyLong(), any(HostRoomTypeQueryCondition.class)))
			.thenReturn(new PageImpl<>(List.of(new HostRoomTypeDto(1L, 1L, "Deluxe Room", 2, 100,
				"Description", 1, "http://image.com/1.jpg"))));

		Page<HostRoomTypeDto> roomTypes = roomTypeService.findRoomTypes(1L, new RoomTypeSearchCondition(
			null, 0, 10, null));

		assertThat(roomTypes).isNotNull();
		assertThat(roomTypes.getContent()).hasSize(1);
	}

	@Test
	@DisplayName("룸 타입 상세 조회 성공")
	void findOneRoomType_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.findOneByIdAndAccommodationId(1L, 1L)).thenReturn(Optional.of(roomTypeDto));
		when(roomImageRepository.findByRoomTypeId(1L)).thenReturn(
			List.of(new RoomImageDto(1L, 1L, "http://image.com/1.jpg", 1, true)));

		FindOneRoomTypeResponse response = roomTypeService.findOneRoomType(1L, 1L);

		assertThat(response).isNotNull();
		assertThat(response.roomType().name()).isEqualTo("Deluxe Room");
		assertThat(response.roomImages()).hasSize(1);
	}
}
