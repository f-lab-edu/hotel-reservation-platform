package com.reservation.batch.job.openroomavailability.processor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.JpaRoomAvailabilityRepository;
import com.reservation.batch.repository.JpaRoomPricingPolicyRepository;
import com.reservation.batch.repository.JpaRoomTypeRepository;
import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.batch.utils.Perf;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ImprovedOpenAvailabilityChunkProcessor
	implements ItemProcessor<List<RoomAutoAvailabilityPolicy>, List<RoomAvailability>> {

	private static final int MAX_PLUS_DAYS = 180;

	private final JpaRoomTypeRepository roomTypeRepository;
	private final JpaRoomAvailabilityRepository availabilityRepository;
	private final JpaRoomPricingPolicyRepository pricingPolicyRepository;

	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	private final LocalDate today = LocalDate.of(2025, 5, 4);
	private final LocalDate endDay = today.plusDays(MAX_PLUS_DAYS);

	@Override
	public List<RoomAvailability> process(List<RoomAutoAvailabilityPolicy> inputAutoPolicies) {
		if (jobExecution.isStopping()) {
			log.error("Job execution stopped {}", jobExecution.getExitStatus().getExitDescription());
			throw ErrorCode.CONFLICT.exception("Job 중단 요청됨 → Reader 중단");
		}

		if (inputAutoPolicies.isEmpty()) {
			return null;
		}

		Perf perf = new Perf();

		// 날짜별로 RoomAvailability 생성
		List<RoomAvailability> outputAvailabilities = createAvailabilitiesSetPeriod(inputAutoPolicies);

		perf.log("Output rows", outputAvailabilities.size());

		return outputAvailabilities;
	}

	private List<RoomAvailability> createAvailabilitiesSetPeriod(List<RoomAutoAvailabilityPolicy> inputAutoPolicies) {
		AutoPolicyRelatedInfo autoPolicyRelatedInfo = findAutoPolicyRelatedInfo(inputAutoPolicies);

		// 성능 최적화★ for -> 병렬 stream 처리
		return IntStream.range(0, (int)ChronoUnit.DAYS.between(today, endDay))
			.parallel()
			.mapToObj(offset -> createAvailabilitiesMatchDate(
				today.plusDays(offset),
				autoPolicyRelatedInfo.findRoomTypes,
				autoPolicyRelatedInfo.roomAutoPolicyMap,
				autoPolicyRelatedInfo.existingDateAvailabilities,
				autoPolicyRelatedInfo.roomPricingMap))
			.flatMap(List::stream)
			.toList();
	}

	// Availability 생성 시 필요한 정보를 위한 AutoPolicy 관련 정보 조회
	private AutoPolicyRelatedInfo findAutoPolicyRelatedInfo(List<RoomAutoAvailabilityPolicy> inputAutoPolicies) {
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

		// 필터링 최적화★ 예약 오픈 정책 List -> Map 변환
		Map<Long, RoomAutoAvailabilityPolicy> roomAutoPolicyMap = inputAutoPolicies.stream()
			.collect(Collectors.toMap(RoomAutoAvailabilityPolicy::getRoomTypeId,
				policy -> policy,
				(policy1, policy2) -> policy1 // 혹시 키 중복 시 처리 (정책상 없어야 함)
			));  // O(N) 변환

		// 필터링 최적화★ 예약 날짜 오픈 존재 여부 체크용 List -> Set 변환 (Key: "{roomTypeId}_{날짜}")
		Set<String> existingDateAvailabilities = existingAvailabilities.stream()
			.map(r -> r.roomTypeId() + "_" + r.date().toString())
			.collect(Collectors.toSet()); // O(N) 변환

		// 필터링 최적화★ 요일별 가격 정책 조회용 List -> Map 변환 (Key: "{roomTypeId}_{요일}", Value: 가격)
		Map<String, Integer> roomPricingMap = registeredPricingPolicies.stream()
			.collect(Collectors.toMap(
				p -> p.getRoomTypeId() + "_" + p.getDayOfWeek().name(), // Key
				RoomPricingPolicy::getPrice,  // Value
				(price1, price2) -> price1 // 혹시 키 중복 시 처리 (정책상 없어야 함)
			)); // O(N) 변환

		return new AutoPolicyRelatedInfo(findRoomTypes, roomAutoPolicyMap, existingDateAvailabilities, roomPricingMap);
	}

	private record AutoPolicyRelatedInfo(
		List<RoomType> findRoomTypes,
		Map<Long, RoomAutoAvailabilityPolicy> roomAutoPolicyMap,
		Set<String> existingDateAvailabilities,
		Map<String, Integer> roomPricingMap
	) {
	}

	// 미래 날짜별 예약 생성
	public List<RoomAvailability> createAvailabilitiesMatchDate(
		LocalDate settingDate,
		List<RoomType> findRoomTypes,
		Map<Long, RoomAutoAvailabilityPolicy> roomAutoPolicyMap,
		Set<String> existingDateAvailabilities,
		Map<String, Integer> roomPricingMap
	) {
		List<RoomAvailability> createAvailabilities = new ArrayList<>(findRoomTypes.size());

		for (RoomType roomType : findRoomTypes) {
			// 예약 오픈 생성 가능한 날짜인지 검증
			boolean validateSettingDateCreateAvailability =
				validateSettingDateCreateAvailability(
					settingDate,
					roomType,
					roomAutoPolicyMap,
					existingDateAvailabilities);

			if (!validateSettingDateCreateAvailability) {
				continue;
			}

			// 요일별 가격 정책 조회 -> 없다면 기본 RoomType 가격 적용
			int roomPrice = findSettingDatePrice(settingDate, roomType, roomPricingMap);

			RoomAvailability newAvailability = RoomAvailability.builder()
				.roomTypeId(roomType.getId())
				.availableCount(roomType.getCapacity())
				.price(roomPrice)
				.date(settingDate)
				.build();

			createAvailabilities.add(newAvailability);
		}

		return createAvailabilities;
	}

	// 예약 생성이 가능한 날짜인지 검증
	private boolean validateSettingDateCreateAvailability(
		LocalDate settingDate,
		RoomType roomType,
		Map<Long, RoomAutoAvailabilityPolicy> roomAutoPolicyMap,
		Set<String> existingDateAvailabilities
	) {
		// 필터링 최적화★ 예약 오픈 정책 Map
		// 설정 된 정책 상 자동 예약 오픈 기간을 벗어난 경우, Availability 등록 불가
		if (!roomAutoPolicyMap.containsKey(roomType.getId())) {
			throw ErrorCode.CONFLICT.exception("Mismatch AutoPolicy & RoomType");
		}
		RoomAutoAvailabilityPolicy checkAutoPolicy = roomAutoPolicyMap.get(roomType.getId());
		if (today.plusDays(checkAutoPolicy.getOpenDaysAhead()).isBefore(settingDate)) {
			return false;
		}

		// 필터링 최적화★ 예약 날짜 오픈 존재 여부 체크용 Set (Key: "{roomTypeId}_{날짜}")
		String existingDateAvailabilityKey = roomType.getId() + "_" + settingDate.toString();
		return !existingDateAvailabilities.contains(existingDateAvailabilityKey);
	}

	// 요일별 가격 정책 찾기
	private int findSettingDatePrice(
		LocalDate settingDate,
		RoomType roomType,
		Map<String, Integer> roomPricingMap
	) {
		// 필터링 최적화★ 가격 조회용 Map (Key: "{roomTypeId}_{요일}", Value: 가격)
		String roomPricingKey = roomType.getId() + "_" + settingDate.getDayOfWeek().name();
		if (roomPricingMap.containsKey(roomPricingKey)) {
			return roomPricingMap.get(roomPricingKey);
		}

		return roomType.getPrice();
	}
}
