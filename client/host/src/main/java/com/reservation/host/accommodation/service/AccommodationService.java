package com.reservation.host.accommodation.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.reservation.domain.accommodation.Accommodation;
import com.reservation.domain.accommodation.Location;
import com.reservation.domain.host.Host;
import com.reservation.host.accommodation.repository.JpaAccommodationRepository;
import com.reservation.host.accommodation.service.dto.DefaultAccommodationInfo;
import com.reservation.host.host.repository.JpaHostRepository;
import com.reservation.support.exception.ErrorCode;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccommodationService {
	private final JpaAccommodationRepository jpaAccommodationRepository;
	private final JpaHostRepository jpaHostRepository;

	@Transactional
	public long createAccommodation(DefaultAccommodationInfo createAccommodationInfo, long hostId) {
		// 이미 숙소가 등록된 호스트인지 확인 (호스트:숙소 = 1:1)
		checkHostAccommodation(hostId);

		// 숙소명 & 숙소 위치 중복 확인
		checkAccommodationNameAndLocation(createAccommodationInfo.name(), createAccommodationInfo.location());

		Host findHost = jpaHostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트를 찾을 수 없습니다."));

		Accommodation newAccommodation = Accommodation.builder()
			.hostId(findHost.getId())
			.name(createAccommodationInfo.name())
			.descriptionOrNull(createAccommodationInfo.descriptionOrNull())
			.location(createAccommodationInfo.location())
			.isVisible(createAccommodationInfo.isVisible())
			.contactNumber(createAccommodationInfo.contactNumber())
			.mainImageUrlOrNull(createAccommodationInfo.mainImageUrlOrNull())
			.build();

		return jpaAccommodationRepository.save(newAccommodation).getId();
	}

	private void checkHostAccommodation(Long hostId) {
		if (jpaAccommodationRepository.existsByHostId(hostId)) {
			throw ErrorCode.CONFLICT.exception("숙소는 1개만 등록할 수 있습니다.");
		}
	}

	private void checkAccommodationNameAndLocation(String name, Location location) {
		if (jpaAccommodationRepository.existsByNameAndLocation(name, location)) {
			throw ErrorCode.CONFLICT.exception("숙소명과 숙소 위치는 중복될 수 없습니다.");
		}
	}

	@Transactional
	public long updateAccommodation(
		DefaultAccommodationInfo updateAccommodationInfo,
		long accommodationId,
		long hostId
	) {
		// 숙소 id & Host id 확인
		checkAccommodationIdAndHostId(accommodationId, hostId);

		// 숙소명 & 숙소 위치 중복 확인
		selfAccommodationNameAndLocation(
			accommodationId, updateAccommodationInfo.name(), updateAccommodationInfo.location());

		Host host = jpaHostRepository.findById(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("호스트를 찾을 수 없습니다."));

		Accommodation updateAccommodation = Accommodation.builder()
			.id(accommodationId)
			.hostId(host.getId())
			.name(updateAccommodationInfo.name())
			.descriptionOrNull(updateAccommodationInfo.descriptionOrNull())
			.location(updateAccommodationInfo.location())
			.isVisible(updateAccommodationInfo.isVisible())
			.contactNumber(updateAccommodationInfo.contactNumber())
			.mainImageUrlOrNull(updateAccommodationInfo.mainImageUrlOrNull())
			.build();

		return jpaAccommodationRepository.save(updateAccommodation).getId();
	}

	private void checkAccommodationIdAndHostId(long accommodationId, long hostId) {
		if (!jpaAccommodationRepository.existsByIdAndHostId(accommodationId, hostId)) {
			throw ErrorCode.CONFLICT.exception("업체 정보와 숙소 정보가 일치하지 않습니다.");
		}
	}

	private void selfAccommodationNameAndLocation(long accommodationId, String name, Location location) {
		Optional<Accommodation> findAccommodation = jpaAccommodationRepository.findOneByNameAndLocation(name, location);

		// 이미 존재하는 숙소명 & 숙소 위치가 있는지 확인 => 동일한 숙소일 경우 허용
		if (findAccommodation.isPresent() && !Objects.equals(findAccommodation.get().getId(), accommodationId)) {
			throw ErrorCode.BAD_REQUEST.exception("숙소명과 숙소 위치는 중복될 수 없습니다.");
		}
	}

	public Accommodation findHostAccommodation(long hostId) {
		return jpaAccommodationRepository.findOneByHostId(hostId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("해당 업체의 숙소를 찾을 수 없습니다."));
	}
}
