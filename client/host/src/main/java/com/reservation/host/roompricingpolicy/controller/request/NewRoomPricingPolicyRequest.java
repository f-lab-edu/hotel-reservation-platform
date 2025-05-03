package com.reservation.host.roompricingpolicy.controller.request;

import java.time.DayOfWeek;

import com.reservation.host.roompricingpolicy.service.dto.DefaultRoomPricingPolicyInfo;
import com.reservation.support.exception.ErrorCode;

public record NewRoomPricingPolicyRequest(
	DayOfWeek dayOfWeek,
	int price
) {
	public DefaultRoomPricingPolicyInfo validateToDefaultRoomPricingPolicyInfo() {
		if (dayOfWeek == null) {
			throw ErrorCode.BAD_REQUEST.exception("요일 정보가 없습니다.");
		}
		if (price < 1000) {
			throw ErrorCode.BAD_REQUEST.exception("가격은 1,000원 이상이어야 합니다.");
		}

		return new DefaultRoomPricingPolicyInfo(dayOfWeek, price);
	}
}
