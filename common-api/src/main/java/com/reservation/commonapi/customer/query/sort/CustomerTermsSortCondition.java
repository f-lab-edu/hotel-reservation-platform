package com.reservation.commonapi.customer.query.sort;

import org.springframework.data.domain.Sort;

import com.reservation.commonmodel.sort.SortCondition;

import jakarta.annotation.Nonnull;

public record CustomerTermsSortCondition(
	@Nonnull CustomerTermsSortField field,
	@Nonnull Sort.Direction direction
) implements SortCondition<CustomerTermsSortField> {
}

