package com.reservation.batch.job.openroomavailability.processor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

import com.reservation.batch.repository.JpaRoomAvailabilityRepository;
import com.reservation.batch.repository.JpaRoomPricingPolicyRepository;
import com.reservation.batch.repository.JpaRoomRepository;
import com.reservation.batch.repository.dto.FindAvailabilityInRoomIdsResult;
import com.reservation.domain.room.Room;
import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;
import com.reservation.domain.roomavailability.RoomAvailability;
import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;

import lombok.RequiredArgsConstructor;

@Component
@StepScope
@RequiredArgsConstructor
public class RoomAutoAvailabilityPolicyListProcessor {
	private static final int MAX_PLUS_DAYS = 180;

	private final JpaRoomRepository jpaRoomRepository;
	private final JpaRoomAvailabilityRepository jpaRoomAvailabilityRepository;
	private final JpaRoomPricingPolicyRepository jpaRoomPricingPolicyRepository;

	public List<RoomAvailability> process(List<RoomAutoAvailabilityPolicy> policies) {
		List<Long> roomIds = policies.stream()
			.map(RoomAutoAvailabilityPolicy::getRoomId)
			.toList();

		LocalDate today = LocalDate.now();
		LocalDate endDay = today.plusDays(MAX_PLUS_DAYS);

		// Room 정보 조회
		List<Room> rooms = jpaRoomRepository.findByIdIn(roomIds);

		// 이미 등록되어 있는 RoomAvailability 리스트 조회
		List<FindAvailabilityInRoomIdsResult> findAvailabilityInRoomIdsResults =
			jpaRoomAvailabilityRepository.findExistingDatesByRoomIds(roomIds, today, endDay);

		// 요일별 가격 정책 조회
		List<RoomPricingPolicy> roomPricingPolicies = jpaRoomPricingPolicyRepository.findByRoomIdIn(roomIds);

		// 날짜별로 Create RoomAvailability
		return IntStream.range(0, (int)ChronoUnit.DAYS.between(today, endDay))
			.parallel()
			.mapToObj(offset -> {
				LocalDate date = today.plusDays(offset);
				return createRoomAvailabilitiesMatchDateAndExisted(
					date, rooms, findAvailabilityInRoomIdsResults, roomPricingPolicies);
			})
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	public List<RoomAvailability> createRoomAvailabilitiesMatchDateAndExisted(
		LocalDate date,
		List<Room> rooms,
		List<FindAvailabilityInRoomIdsResult> findAvailabilityInRoomIdsResults,
		List<RoomPricingPolicy> roomPricingPolicies
	) {
		List<RoomAvailability> result = new ArrayList<>();

		for (Room room : rooms) {
			// 이미 등록된 RoomAvailability skip
			DayOfWeek dayOfWeek = date.getDayOfWeek();
			if (findAvailabilityInRoomIdsResults.stream()
				.anyMatch(findResult ->
					findResult.roomId() == room.getId() && findResult.date().equals(date))) {
				continue;
			}

			int defaultPrice = room.getPrice();

			// 요일별 가격 정책 조회
			RoomPricingPolicy roomPricingPolicy = roomPricingPolicies.stream()
				.filter(policy -> policy.getRoomId() == room.getId())
				.filter(policy -> policy.getDayOfWeek() == dayOfWeek)
				.findFirst()
				.orElse(null);

			int price = roomPricingPolicy != null ? roomPricingPolicy.getPrice() : defaultPrice;

			RoomAvailability newRoomAvailability = RoomAvailability.builder()
				.roomId(room.getId())
				.availableCount(room.getCapacity())
				.price(price)
				.date(date)
				.build();

			result.add(newRoomAvailability);
		}

		return result;
	}
}
