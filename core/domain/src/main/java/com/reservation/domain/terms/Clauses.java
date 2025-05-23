package com.reservation.domain.terms;

import java.util.List;
import java.util.stream.Stream;

import com.reservation.support.exception.ErrorCode;

public record Clauses(List<Clause> values) {

	public Clauses(List<Clause> values) {
		if (values == null || values.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("조항은 최소 1개 이상이어야 합니다.");
		}
		this.values = List.copyOf(values); // 불변화
	}

	// 필요한 메서드 추가
	public int size() {
		return values.size();
	}

	public Clause get(int index) {
		return values.get(index);
	}

	public Stream<Clause> stream() {
		return values.stream();
	}

	public boolean isEmpty() {
		return values.isEmpty();
	}
}
