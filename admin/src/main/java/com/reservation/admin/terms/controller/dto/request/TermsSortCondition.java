package com.reservation.admin.terms.controller.dto.request;

import org.springframework.data.domain.Sort.Direction;

import com.reservation.commonmodel.sort.SortCondition;
import com.reservation.commonmodel.terms.TermsSortField;

public record TermsSortCondition(
	TermsSortField field,
	Direction direction
) implements SortCondition<TermsSortField> {
}
