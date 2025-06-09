package com.reservation.domain.roomavailabilitysummary;

import com.reservation.support.exception.ErrorCode;

public record StayStat(int availableCount, int totalPrice) {
	public StayStat decreaseCount() {
		if (availableCount <= 0)
			throw ErrorCode.VALIDATION_ERROR.exception("수량 부족");
		return new StayStat(availableCount - 1, totalPrice);
	}

	public StayStat increaseCount() {
		return new StayStat(availableCount + 1, totalPrice);
	}
}
