package com.reservation.host.accommodation.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostModuleRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.LocationDto;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.commonmodel.host.HostStatus;
import com.reservation.host.accommodation.controller.dto.request.CreateAccommodationRequest;
import com.reservation.host.accommodation.controller.dto.request.UpdateAccommodationRequest;

class AccommodationServiceTest {

	@Mock
	private HostAccommodationRepository accommodationRepository;

	@Mock
	private HostModuleRepository hostRepository;

	@InjectMocks
	private AccommodationService accommodationService;

	private CreateAccommodationRequest createRequest;
	private UpdateAccommodationRequest updateRequest;
	private HostDto hostDto;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		createRequest = new CreateAccommodationRequest(
			"Test Accommodation", null, "Test Address", 37.7749, 127.4194, true, null, "010-1234-5678"
		);

		updateRequest = new UpdateAccommodationRequest(
			1L, 1L, "Updated Accommodation", null, "Updated Address", 37.7749, 127.4194, true, null, "010-5678-1234"
		);

		hostDto = new HostDto(1L, "test@example.com", HostStatus.ACTIVE, "12341234");
	}

	@Test
	@DisplayName("숙소 생성 성공")
	void createAccommodation_Success() {
		when(hostRepository.findById(1L)).thenReturn(Optional.of(hostDto));
		when(accommodationRepository.existsByHostId(1L)).thenReturn(false);
		when(accommodationRepository.existsByNameAndLocation(anyString(), any(LocationDto.class))).thenReturn(false);
		when(accommodationRepository.save(any(AccommodationDto.class))).thenReturn(
			new AccommodationDto(1L, hostDto, "Test Accommodation", null, null, true, null, "010-1234-5678"));

		Long accommodationId = accommodationService.createAccommodation(createRequest, 1L);

		assertThat(accommodationId).isEqualTo(1L);
		verify(accommodationRepository).save(any(AccommodationDto.class));
	}

	@Test
	@DisplayName("숙소 생성 실패 - 등록 권한 없음")
	void createAccommodation_ThrowsExceptionWhenHostIdMismatch() {
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			accommodationService.createAccommodation(createRequest, 2L);
		});

		assertThat(exception.getMessage()).isEqualTo("호스트를 찾을 수 없습니다.");
	}

	@Test
	@DisplayName("숙소 생성 실패 - 이미 등록된 숙소")
	void createAccommodation_ThrowsExceptionWhenHostAlreadyHasAccommodation() {
		when(accommodationRepository.existsByHostId(1L)).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			accommodationService.createAccommodation(createRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소는 1개만 등록할 수 있습니다.");
	}

	@Test
	@DisplayName("숙소 생성 실패 - 숙소명 및 위치 중복")
	void createAccommodation_ThrowsExceptionWhenNameAndLocationDuplicate() {
		when(accommodationRepository.existsByNameAndLocation(anyString(), any(LocationDto.class))).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			accommodationService.createAccommodation(createRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소명과 숙소 위치는 중복될 수 없습니다.");
	}

	@Test
	@DisplayName("숙소 수정 성공")
	void updateAccommodation_Success() {
		when(hostRepository.findById(1L)).thenReturn(Optional.of(hostDto));
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(true);
		when(accommodationRepository.findOneByNameAndLocation(anyString(), any(LocationDto.class))).thenReturn(
			Optional.empty());
		when(accommodationRepository.save(any(AccommodationDto.class))).thenReturn(
			new AccommodationDto(1L, hostDto, "Updated Accommodation", null, null, true, null, "010-5678-1234"));

		Long accommodationId = accommodationService.updateAccommodation(updateRequest, 1L);

		assertThat(accommodationId).isEqualTo(1L);
		verify(accommodationRepository).save(any(AccommodationDto.class));
	}

	@Test
	@DisplayName("숙소 수정 실패 - 수정 권한 없음")
	void updateAccommodation_ThrowsExceptionWhenHostIdMismatch() {
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			accommodationService.updateAccommodation(updateRequest, 2L);
		});

		assertThat(exception.getMessage()).isEqualTo("수정 권한이 없는 호스트입니다.");
	}

	@Test
	@DisplayName("숙소 수정 실패 - 숙소 존재하지 않음")
	void updateAccommodation_ThrowsExceptionWhenAccommodationNotFound() {
		when(accommodationRepository.existsByIdAndHostId(1L, 1L)).thenReturn(false);

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			accommodationService.updateAccommodation(updateRequest, 1L);
		});

		assertThat(exception.getMessage()).isEqualTo("수정 할 숙소가 존재하지 않습니다.");
	}

	@Test
	@DisplayName("숙소 조회 성공")
	void findHostAccommodation_Success() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.of(
			new AccommodationDto(1L, hostDto, "Test Accommodation", null, null, true, null, "010-1234-5678")));

		AccommodationDto accommodation = accommodationService.findHostAccommodation(1L);

		assertThat(accommodation).isNotNull();
		assertThat(accommodation.name()).isEqualTo("Test Accommodation");
	}

	@Test
	@DisplayName("숙소 조회 실패 - 숙소 없음")
	void findHostAccommodation_ThrowsExceptionWhenNotFound() {
		when(accommodationRepository.findByHostId(1L)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			accommodationService.findHostAccommodation(1L);
		});

		assertThat(exception.getMessage()).isEqualTo("숙소를 찾을 수 없습니다.");
	}
}
