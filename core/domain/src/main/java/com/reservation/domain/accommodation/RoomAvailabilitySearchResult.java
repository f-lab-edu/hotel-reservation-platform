package com.reservation.domain.accommodation;

public record RoomAvailabilitySearchResult(
	Long accommodationId,
	String accommodationName,
	Integer averagePrice
) {
}
