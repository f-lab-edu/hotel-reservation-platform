package com.reservation.querysupport.page;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.reservation.querysupport.sort.SortCondition;

public interface PageableRequest<T extends SortCondition> {
	Integer page();

	Integer size();

	List<T> sorts();

	default PageRequest toPageRequest() {
		int page = this.page() != null ? this.page() : 0;
		int size = this.size() != null ? this.size() : this.defaultPageSize();
		Sort sort = this.toSort();
		return PageRequest.of(page, size, sort);
	}

	default Sort toSort() {
		List<T> sorts = sorts();

		if (sorts == null || sorts.isEmpty()) {
			return Sort.by(getDefaultSortOrder());
		}

		List<Sort.Order> orders = sorts.stream()
			.map(sort -> new Sort.Order(sort.direction(), sort.field().getFieldName()))
			.toList();

		return Sort.by(orders);
	}

	Sort.Order getDefaultSortOrder();

	int defaultPageSize();
}
