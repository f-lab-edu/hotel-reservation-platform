package com.reservation.customer.roomavailability.controller;

import static com.reservation.support.response.ApiResponse.*;

import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reservation.customer.roomavailability.controller.request.SearchAvailableRoomCondition;
import com.reservation.customer.roomavailability.repository.dto.SearchAvailableRoomSortField;
import com.reservation.customer.roomavailability.service.RoomAvailabilityService;
import com.reservation.customer.roomavailability.service.dto.DefaultRoomAvailabilitySearchInfo;
import com.reservation.domain.accommodation.RoomAvailabilitySearchResult;
import com.reservation.support.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/no-auth/room-availability")
@Tag(name = "룸 예약 가용 정보 API", description = "룸 예약 가용 정보 조회 API 입니다.")
@RequiredArgsConstructor
public class RoomAvailabilityController {
	private final RoomAvailabilityService availabilityService;

	@GetMapping()
	@Operation(summary = "예약 가능한 숙소 조회", description = "일반 사용자가 예약 가능한 숙소를 조회합니다.")
	public ApiResponse<PageImpl<RoomAvailabilitySearchResult>> searchRoomAvailability(
		@ModelAttribute SearchAvailableRoomCondition condition
	) {
		DefaultRoomAvailabilitySearchInfo searchInfo = condition.validateToSearchInfo();
		int page = condition.page();
		int size = condition.size();
		SearchAvailableRoomSortField sortField = condition.sortField();

		PageImpl<RoomAvailabilitySearchResult> searchResults =
			availabilityService.searchRoomAvailability(searchInfo, sortField, page, size);

		return ok(searchResults);
	}
}
