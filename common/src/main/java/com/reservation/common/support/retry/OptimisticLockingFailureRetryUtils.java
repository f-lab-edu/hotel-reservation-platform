package com.reservation.common.support.retry;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;

import com.reservation.common.exception.ErrorCode;

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
				throw ErrorCode.INTERNAL_SERVER_ERROR.exception("서버 내부 오류로 인한 작업 실패, 재시도 요청 필요");
			}

			return executeWithRetry(maxRetryCount, operation, retryCount);
		} catch (DataIntegrityViolationException e) {
			log.error("데이터 무결성 위반 - 작업 실패", e);
			throw ErrorCode.CONFLICT.exception("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요");
		}
	}

	@FunctionalInterface
	public interface RetryableOperation<T> {
		T execute() throws OptimisticLockingFailureException;
	}
}
