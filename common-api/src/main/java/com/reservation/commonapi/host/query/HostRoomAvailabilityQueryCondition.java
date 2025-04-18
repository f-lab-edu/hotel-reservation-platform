package com.reservation.commonapi.host.query;

import java.time.LocalDate;

public record HostRoomAvailabilityQueryCondition(
	Long roomTypeId,
	LocalDate startDate,
	LocalDate endDate
) {
}
