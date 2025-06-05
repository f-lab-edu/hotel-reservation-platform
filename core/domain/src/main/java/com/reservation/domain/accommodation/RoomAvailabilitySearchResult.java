package com.reservation.domain.accommodation;

public record RoomAvailabilitySearchResult(
	Long accommodationId,
	Long roomTypeId,
	String accommodationName,
	Integer totalPrice
) {
}
