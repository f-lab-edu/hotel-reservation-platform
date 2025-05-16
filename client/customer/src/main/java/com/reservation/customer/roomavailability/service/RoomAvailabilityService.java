package com.reservation.customer.roomavailability.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import com.reservation.customer.roomavailability.repository.RoomAvailabilityQueryRepository;
import com.reservation.customer.roomavailability.repository.dto.SearchAvailableRoomSortField;
import com.reservation.customer.roomavailability.service.dto.DefaultRoomAvailabilitySearchInfo;
import com.reservation.domain.accommodation.RoomAvailabilitySearchResult;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {
	private final RoomAvailabilityQueryRepository availabilityQueryRepository;

	public PageImpl<RoomAvailabilitySearchResult> searchRoomAvailability(
		DefaultRoomAvailabilitySearchInfo searchInfo,
		SearchAvailableRoomSortField sortField,
		int page,
		int size
	) {
		LocalDate checkIn = searchInfo.checkIn();
		LocalDate checkOut = searchInfo.checkOut();
		int capacity = searchInfo.capacity();
		long requiredDayCount = ChronoUnit.DAYS.between(checkIn, checkOut);

		return availabilityQueryRepository.searchRoomAvailability(
			checkIn, checkOut, requiredDayCount, capacity, sortField, page, size);
	}
}
