package com.reservation.batch.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Perf {
	private long t = System.currentTimeMillis();

	public void log(String msg, long value) {
		long now = System.currentTimeMillis();
		log.info("[PERF] {}={} took {} ms", msg, value, now - t);
		t = now;
	}
}
