package com.reservation.querysupport.sort;

import org.springframework.data.domain.Sort;

public interface SortCondition<T extends SortField> {
	T field();

	Sort.Direction direction();
}
