package com.reservation.customer.terms.controller.request;

import org.springframework.data.domain.Sort;

import com.reservation.querysupport.sort.SortCondition;

public record TermsSortCondition(
	TermsSortField field,
	Sort.Direction direction
) implements SortCondition<TermsSortField> {
}
