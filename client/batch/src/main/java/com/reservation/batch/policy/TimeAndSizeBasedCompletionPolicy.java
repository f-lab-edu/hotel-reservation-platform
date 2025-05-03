package com.reservation.batch.policy;

import java.util.List;

import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.RepeatStatus;

public class TimeAndSizeBasedCompletionPolicy implements CompletionPolicy {

	private final long timeoutMillis;
	private final int maxItemCount;

	private long chunkStartTimeMillis;
	private int itemCount = 0;

	public TimeAndSizeBasedCompletionPolicy(long timeoutSeconds, int maxItemCount) {
		this.timeoutMillis = timeoutSeconds * 1000L;
		this.maxItemCount = maxItemCount;
	}

	@Override
	public RepeatContext start(RepeatContext parent) {
		this.chunkStartTimeMillis = System.currentTimeMillis();
		this.itemCount = 0;
		return parent;
	}

	@Override
	public void update(RepeatContext context) {
		Object item = context.getAttribute("item");
		if (item == null) {
			return; // 더 이상 처리할게 없음
		}
		if (item instanceof List<?> list) {
			itemCount += list.size(); // 리스트 크기만큼 증가
		} else {
			itemCount++;
		}
	}

	@Override
	public boolean isComplete(RepeatContext context) {
		return isCompleteInternal();
	}

	@Override
	public boolean isComplete(RepeatContext context, RepeatStatus result) {
		return isCompleteInternal();
	}

	private boolean isCompleteInternal() {
		long currentTime = System.currentTimeMillis();
		boolean timeoutExceeded = (currentTime - chunkStartTimeMillis) >= timeoutMillis;
		boolean sizeExceeded = itemCount >= maxItemCount;
		return timeoutExceeded || sizeExceeded;
	}
}
