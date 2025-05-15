package com.reservation.batch.job.openroomavailability.taskletStep.processor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.JpaOriginRoomAvailabilityRepository;
import com.reservation.batch.repository.JpaRoomPricingPolicyRepository;
import com.reservation.batch.repository.JpaRoomTypeRepository;
import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.OriginRoomAvailability;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class OriginOpenAvailabilityTaskletProcessor {
	private static final int MAX_PLUS_DAYS = 180;

	private final JpaRoomTypeRepository roomTypeRepository;
	private final JpaOriginRoomAvailabilityRepository availabilityRepository;
	private final JpaRoomPricingPolicyRepository pricingPolicyRepository;

	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	// private final LocalDate today = LocalDate.now();
	private final LocalDate today = LocalDate.of(2025, 5, 4);
	private final LocalDate endDay = today.plusDays(MAX_PLUS_DAYS);

	public List<OriginRoomAvailability> process(List<RoomAutoAvailabilityPolicy> inputAutoPolicies) {
		if (jobExecution.isStopping()) {
			log.error("Job execution stopped {}", jobExecution.getExitStatus().getExitDescription());
			throw ErrorCode.CONFLICT.exception("Job 중단 요청됨 → Reader 중단");
		}

		if (inputAutoPolicies.isEmpty()) {
			return null;
		}
		// 날짜별로 RoomAvailability 생성
		List<OriginRoomAvailability> outputAvailabilities = createAvailabilitiesSetPeriod(inputAutoPolicies);
		return outputAvailabilities;
	}

	private List<OriginRoomAvailability> createAvailabilitiesSetPeriod(
		List<RoomAutoAvailabilityPolicy> inputAutoPolicies) {
		AutoPolicyRelatedInfo autoPolicyRelatedInfo = findAutoPolicyRelatedInfo(inputAutoPolicies);

		List<OriginRoomAvailability> outputAvailabilities = new ArrayList<>(
			inputAutoPolicies.size() * MAX_PLUS_DAYS / 2);

		// 최대 예약 오픈 기간 범위로 RoomAvailability 생성
		for (LocalDate settingDate = today; settingDate.isBefore(endDay); settingDate = settingDate.plusDays(1)) {
			List<OriginRoomAvailability> createAvailabilities =
				createAvailabilitiesMatchDate(
					settingDate,
					autoPolicyRelatedInfo.findRoomTypes,
					inputAutoPolicies,
					autoPolicyRelatedInfo.existingAvailabilities,
					autoPolicyRelatedInfo.registeredPricingPolicies);

			outputAvailabilities.addAll(createAvailabilities);
		}

		return outputAvailabilities;
	}

	// Availability 생성 시 필요한 정보를 위한 AutoPolicy 관련 정보 조회
	private AutoPolicyRelatedInfo findAutoPolicyRelatedInfo(
		List<RoomAutoAvailabilityPolicy> inputAutoPolicies) {
		List<Long> roomTypeIds = inputAutoPolicies.stream()
			.map(RoomAutoAvailabilityPolicy::getRoomTypeId)
			.toList();

		// RoomType 정보 조회
		List<RoomType> findRoomTypes = roomTypeRepository.findByIdIn(roomTypeIds);
		if (roomTypeIds.size() != findRoomTypes.size()) {
			throw ErrorCode.CONFLICT.exception("Mismatch RoomType");
		}

		// 등록된 요일별 가격 정책 조회
		List<RoomPricingPolicy> registeredPricingPolicies = pricingPolicyRepository.findByRoomTypeIdIn(roomTypeIds);

		// 이미 존재하는 RoomAvailability 리스트 조회
		List<FindAvailabilityInRoomIdsResult> existingAvailabilities =
			availabilityRepository.findExistingDatesByRoomIds(roomTypeIds, today, endDay);

		return new AutoPolicyRelatedInfo(findRoomTypes, registeredPricingPolicies, existingAvailabilities);
	}

	private record AutoPolicyRelatedInfo(
		List<RoomType> findRoomTypes,
		List<RoomPricingPolicy> registeredPricingPolicies,
		List<FindAvailabilityInRoomIdsResult> existingAvailabilities
	) {
	}

	// 미래 날짜별 예약 생성
	public List<OriginRoomAvailability> createAvailabilitiesMatchDate(
		LocalDate settingDate,
		List<RoomType> findRoomTypes,
		List<RoomAutoAvailabilityPolicy> inputAutoPolicies,
		List<FindAvailabilityInRoomIdsResult> existingAvailabilities,
		List<RoomPricingPolicy> registeredPricingPolicies
	) {
		List<OriginRoomAvailability> createAvailabilities = new ArrayList<>(findRoomTypes.size());

		for (RoomType roomType : findRoomTypes) {
			// 예약 오픈 생성 가능한 날짜인지 검증
			boolean validateSettingDateCreateAvailability =
				validateSettingDateCreateAvailability(settingDate, roomType, inputAutoPolicies, existingAvailabilities);

			if (!validateSettingDateCreateAvailability) {
				continue;
			}

			// 요일별 가격 정책 조회 -> 없다면 기본 RoomType 가격 적용
			int roomPrice = findSettingDatePrice(settingDate, roomType, registeredPricingPolicies);

			OriginRoomAvailability newAvailability = OriginRoomAvailability.builder()
				.roomTypeId(roomType.getId())
				.availableCount(roomType.getCapacity())
				.price(roomPrice)
				.openDate(settingDate)
				.build();

			createAvailabilities.add(newAvailability);
		}

		return createAvailabilities;
	}

	// 예약 생성이 가능한 날짜인지 검증
	private boolean validateSettingDateCreateAvailability(
		LocalDate settingDate,
		RoomType roomType,
		List<RoomAutoAvailabilityPolicy> inputAutoPolicies,
		List<FindAvailabilityInRoomIdsResult> existingAvailabilities
	) {
		// 설정 된 정책 상 자동 예약 오픈 기간을 벗어난 경우, Availability 등록 불가
		RoomAutoAvailabilityPolicy checkAutoPolicy = inputAutoPolicies.stream()
			.filter(auto -> auto.getRoomTypeId() == roomType.getId())
			.findFirst()
			.orElseThrow(() -> ErrorCode.CONFLICT.exception("RoomAutoAvailabilityPolicy not found"));

		if (today.plusDays(checkAutoPolicy.getOpenDaysAhead()).isBefore(settingDate)) {
			return false;
		}

		// 이미 등록된 Availability 인 경우 등록 불가
		boolean checkExistingAvailability = existingAvailabilities.stream()
			.anyMatch(availability ->
				availability.roomTypeId() == roomType.getId() && availability.date().equals(settingDate));

		return !checkExistingAvailability;
	}

	// 요일별 가격 정책 찾기
	private int findSettingDatePrice(
		LocalDate settingDate,
		RoomType roomType,
		List<RoomPricingPolicy> registeredPricingPolicies
	) {
		RoomPricingPolicy matchPricingPolicy = registeredPricingPolicies.stream()
			.filter(pricing -> pricing.getRoomTypeId() == roomType.getId())
			.filter(pricing -> pricing.getDayOfWeek() == settingDate.getDayOfWeek())
			.findFirst()
			.orElse(null);

		return matchPricingPolicy != null ? matchPricingPolicy.getPrice() : roomType.getPrice();
	}
}
