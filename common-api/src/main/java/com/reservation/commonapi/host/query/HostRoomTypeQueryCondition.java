package com.reservation.commonapi.host.query;

import org.springframework.data.domain.PageRequest;

public record HostRoomTypeQueryCondition(
	String name,
	PageRequest pageRequest
) {
}
