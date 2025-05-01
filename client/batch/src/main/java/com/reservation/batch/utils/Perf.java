package com.reservation.batch.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Perf {
	private long t = System.currentTimeMillis();

	void log(String msg, long value) {
		long now = System.currentTimeMillis();
		log.info("[PERF] {}={} took {} ms", msg, value, now - t);
		t = now;
	}
}
