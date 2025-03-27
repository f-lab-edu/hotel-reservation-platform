package com.reservation.common.support.retry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import com.reservation.common.support.retry.OptimisticLockingFailureRetryUtils.RetryableOperation;

public class OptimisticLockingFailureRetryUtilsTest {
	@Test
	public void 낙관적락재시도_즉시성공() {
		String result = OptimisticLockingFailureRetryUtils.executeWithRetry(3, () -> "Success");
		assertEquals("Success", result);
	}

	@Test
	public void 낙관적락재시도_재시도성공() {
		RetryableOperation<String> operation = new RetryableOperation<>() {
			private int attempt = 0;

			@Override
			public String execute() throws OptimisticLockingFailureException {
				if (attempt < 2) {
					attempt++;
					throw new OptimisticLockingFailureException("Retry");
				}
				return "Success";
			}
		};

		int maxRetryCount = 3;
		String result = OptimisticLockingFailureRetryUtils.executeWithRetry(maxRetryCount, operation);

		assertEquals("Success", result);
	}

	@Test
	public void 낙관적락재시도_재시도초과() {
		RetryableOperation<String> operation = new RetryableOperation<>() {
			@Override
			public String execute() throws OptimisticLockingFailureException {
				throw new OptimisticLockingFailureException("Retry");
			}
		};

		int maxRetryCount = 3;

		assertThrows(OptimisticLockingFailureException.class, () ->
			OptimisticLockingFailureRetryUtils.executeWithRetry(maxRetryCount, operation)
		);
	}
}
