package com.reservation.commonmodel.sort;

import org.springframework.data.domain.Sort.Direction;

public interface SortCondition<T extends SortField> {
	T field();

	Direction direction();
}
