package com.reservation.common.request;

import static org.springframework.data.domain.Sort.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.reservation.commonmodel.sort.SortField;

public interface PageableRequest<T extends SortField> {
	Integer page();

	Integer size();

	List<T> sortFields();

	List<Direction> sortDirections();

	default PageRequest toPageRequest() {
		int page = this.page() != null ? this.page() : 0;
		int size = this.size() != null ? this.size() : this.defaultPageSize();
		Sort sort = this.toSort();
		return PageRequest.of(page, size, sort);
	}

	default Sort toSort() {
		List<T> sortFields = sortFields();
		List<Direction> sortDirections = sortDirections();

		if (sortFields == null || sortFields.isEmpty() || sortDirections == null || sortDirections.isEmpty()
			|| sortFields.size() != sortDirections.size()) {
			return Sort.by(getDefaultSortOrder());
		}

		List<Order> orders = new ArrayList<>();
		for (int i = 0; i < sortFields.size(); i++) {
			if (sortFields.get(i) == null || sortDirections.get(i) == null) {
				continue;
			}
			Order order = new Order(sortDirections.get(i), sortFields.get(i).getFieldName());
			orders.add(order);
		}
		return Sort.by(orders);
	}

	Order getDefaultSortOrder();

	int defaultPageSize();
}
