package com.reservation.common.support.retry;

import org.springframework.dao.OptimisticLockingFailureException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class OptimisticLockingFailureRetryUtils {
	public static <T> T executeWithRetry(int maxRetryCount, RetryableOperation<T> operation) {
		return executeWithRetry(maxRetryCount, operation, 0);
	}

	private static <T> T executeWithRetry(int maxRetryCount, RetryableOperation<T> operation, int retryCount) {
		try {
			return operation.execute();
		} catch (OptimisticLockingFailureException e) {
			retryCount++;
			log.warn("낙관적 락 충돌 발생 - 재시도 {}회", retryCount);

			if (retryCount >= maxRetryCount) {
				log.error("최대 재시도 초과 - 작업 실패", e);
				throw e;
			}

			return executeWithRetry(maxRetryCount, operation, retryCount);
		}
	}

	@FunctionalInterface
	public interface RetryableOperation<T> {
		T execute() throws OptimisticLockingFailureException;
	}
}
