package com.reservation.common.support.sort;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;

public class SortUtils {

	public static <T> List<OrderSpecifier<?>> getOrderSpecifiers(Sort sort, Class<T> domainClass, String alias) {
		PathBuilder<T> pathBuilder = new PathBuilder<>(domainClass, alias);
		List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

		for (Sort.Order order : sort) {
			Expression expression = pathBuilder.get(order.getProperty());
			orderSpecifiers.add(
				new OrderSpecifier<>(
					order.isAscending() ? Order.ASC : Order.DESC,
					expression
				)
			);
		}

		return orderSpecifiers;
	}
}
