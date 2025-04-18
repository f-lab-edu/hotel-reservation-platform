package com.reservation.host.accommodation.service;

import static com.reservation.host.accommodation.service.mapper.AccommodationDtoMapper.*;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.reservation.commonapi.host.repository.HostAccommodationRepository;
import com.reservation.commonapi.host.repository.HostModuleRepository;
import com.reservation.commonmodel.accommodation.AccommodationDto;
import com.reservation.commonmodel.accommodation.LocationDto;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.host.accommodation.controller.dto.request.CreateAccommodationRequest;
import com.reservation.host.accommodation.controller.dto.request.UpdateAccommodationRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccommodationService {
	private final HostAccommodationRepository accommodationRepository;
	private final HostModuleRepository hostRepository;

	@Transactional
	public Long createAccommodation(CreateAccommodationRequest request, Long hostId) {
		// 이미 숙소가 등록된 호스트인지 확인 (호스트:숙소 = 1:1)
		checkHostAccommodation(hostId);

		// 숙소명 & 숙소 위치 중복 확인
		checkAccommodationNameAndLocation(request);

		HostDto host = hostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트를 찾을 수 없습니다."));

		AccommodationDto accommodationDto = fromCreateRequest(request, host);
		return accommodationRepository.save(accommodationDto).id();
	}

	private void checkHostAccommodation(Long hostId) {
		if (accommodationRepository.existsByHostId(hostId)) {
			throw ErrorCode.BAD_REQUEST.exception("숙소는 1개만 등록할 수 있습니다.");
		}
	}

	private void checkAccommodationNameAndLocation(CreateAccommodationRequest request) {
		String name = request.name();
		LocationDto location = new LocationDto(request.address(), request.latitude(), request.longitude());
		if (accommodationRepository.existsByNameAndLocation(name, location)) {
			throw ErrorCode.BAD_REQUEST.exception("숙소명과 숙소 위치는 중복될 수 없습니다.");
		}
	}

	@Transactional
	public Long updateAccommodation(UpdateAccommodationRequest request, Long hostId) {
		if (!request.hostId().equals(hostId)) {
			throw ErrorCode.BAD_REQUEST.exception("수정 권한이 없는 호스트입니다.");
		}
		// 숙소 id & Host id 확인
		checkAccommodationIdAndHostId(request.id(), request.hostId());

		// 숙소명 & 숙소 위치 중복 확인
		selfAccommodationNameAndLocation(request);

		HostDto host = hostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트를 찾을 수 없습니다."));

		AccommodationDto accommodationDto = fromUpdateRequest(request, host);
		return accommodationRepository.save(accommodationDto).id();
	}

	private void checkAccommodationIdAndHostId(Long id, Long hostId) {
		if (!accommodationRepository.existsByIdAndHostId(id, hostId)) {
			throw ErrorCode.BAD_REQUEST.exception("수정 할 숙소가 존재하지 않습니다.");
		}
	}

	private void selfAccommodationNameAndLocation(UpdateAccommodationRequest request) {
		String name = request.name();
		Long id = request.id();
		LocationDto location = new LocationDto(request.address(), request.latitude(), request.longitude());

		Optional<AccommodationDto> accommodationDto = accommodationRepository.findOneByNameAndLocation(name, location);
		if (accommodationDto.isPresent() && !Objects.equals(accommodationDto.get().id(), id)) {
			throw ErrorCode.BAD_REQUEST.exception("숙소명과 숙소 위치는 중복될 수 없습니다.");
		}
	}

	public AccommodationDto findHostAccommodation(Long hostId) {
		return accommodationRepository.findByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("숙소를 찾을 수 없습니다."));
	}
}
