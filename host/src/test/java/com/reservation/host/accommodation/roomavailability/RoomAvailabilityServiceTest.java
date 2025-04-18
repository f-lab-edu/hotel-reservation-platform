package com.reservation.host.accommodation.roomavailability;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reservation.commonapi.host.query.HostRoomAvailabilityQueryCondition;
import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostRoomAvailabilityRepository;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.RoomAvailabilityDto;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.host.accommodation.availability.controller.dto.request.CreateRoomAvailabilityRequest;
import com.reservation.host.accommodation.availability.controller.dto.request.RoomAvailabilitySearchCondition;
import com.reservation.host.accommodation.availability.controller.dto.request.UpdateRoomAvailabilityRequest;
import com.reservation.host.accommodation.availability.service.RoomAvailabilityService;

class RoomAvailabilityServiceTest {

	@Mock
	private HostRoomAvailabilityRepository roomAvailabilityRepository;

	@Mock
	private HostAccommodationRepository accommodationRepository;

	@Mock
	private HostRoomTypeRepository roomTypeRepository;

	@InjectMocks
	private RoomAvailabilityService roomAvailabilityService;

	private AccommodationDto accommodationDto;
	private CreateRoomAvailabilityRequest createRequest;
	private UpdateRoomAvailabilityRequest updateRequest;
	private RoomAvailabilitySearchCondition searchCondition;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		accommodationDto = new AccommodationDto(1L, null, "Test Accommodation", null, null, true, null,
			"010-1234-5678");

		createRequest = new CreateRoomAvailabilityRequest(1L, LocalDate.of(2023, 10, 1), 5);
		updateRequest = new UpdateRoomAvailabilityRequest(1L, 1L, LocalDate.of(2023, 10, 5), 3);
		searchCondition = new RoomAvailabilitySearchCondition(1L, LocalDate.of(2023, 10, 1),
			LocalDate.of(2023, 10, 10));
	}

	@Test
	@DisplayName("룸 가용 정보 생성 성공")
	void createRoomAvailability_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.existsByIdAndAccommodationId(1L, 1L)).thenReturn(true);
		when(roomAvailabilityRepository.save(any(RoomAvailabilityDto.class))).thenReturn(
			new RoomAvailabilityDto(1L, 1L, LocalDate.of(2023, 10, 1), 5));

		Long roomAvailabilityId = roomAvailabilityService.createRoomAvailability(createRequest, 1L);

		assertThat(roomAvailabilityId).isEqualTo(1L);
		verify(roomAvailabilityRepository).save(any(RoomAvailabilityDto.class));
	}

	@Test
	@DisplayName("룸 가용 정보 생성 실패 - 숙소 없음")
	void createRoomAvailability_ThrowsExceptionWhenAccommodationNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomAvailabilityService.createRoomAvailability(createRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소 정보를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("룸 가용 정보 생성 실패 - 룸 타입 없음")
	void createRoomAvailability_ThrowsExceptionWhenRoomTypeNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.existsByIdAndAccommodationId(1L, 1L)).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			roomAvailabilityService.createRoomAvailability(createRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("룸타입 정보를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("룸 가용 정보 수정 성공")
	void updateRoomAvailability_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.existsByIdAndAccommodationId(1L, 1L)).thenReturn(true);
		when(roomAvailabilityRepository.save(any(RoomAvailabilityDto.class))).thenReturn(
			new RoomAvailabilityDto(1L, 1L, LocalDate.of(2023, 10, 5), 3));

		Long roomAvailabilityId = roomAvailabilityService.updateRoomAvailability(updateRequest, 1L);

		assertThat(roomAvailabilityId).isEqualTo(1L);
		verify(roomAvailabilityRepository).save(any(RoomAvailabilityDto.class));
	}

	@Test
	@DisplayName("룸 가용 정보 조회 성공")
	void findRoomAvailability_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(accommodationDto));
		when(roomTypeRepository.existsByIdAndAccommodationId(1L, 1L)).thenReturn(true);
		when(roomAvailabilityRepository.findRoomAvailability(any(HostRoomAvailabilityQueryCondition.class)))
			.thenReturn(
				List.of(new RoomAvailabilityDto(1L, 1L, LocalDate.of(2023, 10, 1), 5)));

		List<RoomAvailabilityDto> roomAvailabilities = roomAvailabilityService.findRoomAvailability(searchCondition,
			1L);

		assertThat(roomAvailabilities).hasSize(1);
		assertThat(roomAvailabilities.get(0).roomTypeId()).isEqualTo(1L);
	}
}
