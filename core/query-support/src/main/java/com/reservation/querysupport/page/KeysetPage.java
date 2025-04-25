package com.reservation.querysupport.page;

import java.util.List;

public record KeysetPage<T, S>(
	List<T> content,
	boolean hasNext,
	List<S> nextCursors
) {
}
