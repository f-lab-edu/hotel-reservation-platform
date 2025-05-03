package com.reservation.batch.job.openroomavailability.processor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.JpaRoomAvailabilityRepository;
import com.reservation.batch.repository.JpaRoomPricingPolicyRepository;
import com.reservation.batch.repository.JpaRoomTypeRepository;
import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;
import com.reservation.domain.roomtype.RoomType;

@Component
@StepScope
public class RoomAutoAvailabilityPolicyListItemProcessor
	implements ItemProcessor<List<RoomAutoAvailabilityPolicy>, List<RoomAvailability>> {

	private static final int MAX_PLUS_DAYS = 180;

	private static JpaRoomTypeRepository jpaRoomTypeRepository;
	private static JpaRoomAvailabilityRepository jpaRoomAvailabilityRepository;
	private static JpaRoomPricingPolicyRepository jpaRoomPricingPolicyRepository;

	@Override
	public List<RoomAvailability> process(List<RoomAutoAvailabilityPolicy> policies) {
		List<RoomAvailability> result = new ArrayList<>();

		List<Long> roomIds = policies.stream()
			.map(RoomAutoAvailabilityPolicy::getRoomTypeId)
			.toList();

		LocalDate today = LocalDate.now();
		LocalDate endDay = today.plusDays(MAX_PLUS_DAYS);

		// Room 정보 조회
		List<RoomType> rooms = jpaRoomTypeRepository.findByIdIn(roomIds);

		// 이미 등록되어 있는 RoomAvailability 리스트 조회
		List<FindAvailabilityInRoomIdsResult> findAvailabilityInRoomIdsResults =
			jpaRoomAvailabilityRepository.findExistingDatesByRoomIds(roomIds, today, endDay);

		// 요일별 가격 정책 조회
		List<RoomPricingPolicy> roomPricingPolicies = jpaRoomPricingPolicyRepository.findByRoomTypeIdIn(roomIds);

		// 날짜별로 RoomAvailability 생성
		for (LocalDate startDate = today; startDate.isBefore(endDay); startDate = startDate.plusDays(1)) {
			List<RoomAvailability> createRoomAvailabilities =
				createRoomAvailabilitiesMatchDateAndExisted(
					startDate, rooms, findAvailabilityInRoomIdsResults, roomPricingPolicies);

			result.addAll(createRoomAvailabilities);
		}

		return result;
	}

	public List<RoomAvailability> createRoomAvailabilitiesMatchDateAndExisted(
		LocalDate date,
		List<RoomType> roomTypes,
		List<FindAvailabilityInRoomIdsResult> findAvailabilityInRoomIdsResults,
		List<RoomPricingPolicy> roomPricingPolicies
	) {
		List<RoomAvailability> result = new ArrayList<>();

		for (RoomType roomType : roomTypes) {
			// 이미 등록된 RoomAvailability skip
			DayOfWeek dayOfWeek = date.getDayOfWeek();
			if (findAvailabilityInRoomIdsResults.stream()
				.anyMatch(findResult ->
					findResult.roomTypeId() == roomType.getId() && findResult.date().equals(date))) {
				continue;
			}

			int defaultPrice = roomType.getPrice();

			// 요일별 가격 정책 조회
			RoomPricingPolicy roomPricingPolicy = roomPricingPolicies.stream()
				.filter(policy -> policy.getRoomTypeId() == roomType.getId())
				.filter(policy -> policy.getDayOfWeek() == dayOfWeek)
				.findFirst()
				.orElse(null);

			int price = roomPricingPolicy != null ? roomPricingPolicy.getPrice() : defaultPrice;

			RoomAvailability newRoomAvailability = RoomAvailability.builder()
				.roomTypeId(roomType.getId())
				.availableCount(roomType.getCapacity())
				.price(price)
				.date(date)
				.build();

			result.add(newRoomAvailability);
		}

		return result;
	}
}
