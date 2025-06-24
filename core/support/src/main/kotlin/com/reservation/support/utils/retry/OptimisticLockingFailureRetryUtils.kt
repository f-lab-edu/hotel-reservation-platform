package com.reservation.support.utils.retry

import com.reservation.support.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException

object OptimisticLockingFailureRetryUtils {
    private val log: Logger = LoggerFactory.getLogger(OptimisticLockingFailureRetryUtils::class.java)

    fun <T> executeWithRetry(maxRetryCount: Int, operation: RetryableOperation<T>): T {
        return executeWithRetry(maxRetryCount, operation, 0)
    }

    private fun <T> executeWithRetry(maxRetryCount: Int, operation: RetryableOperation<T>, retryCount: Int): T {
        var retryCount = retryCount
        try {
            return operation.execute()
        } catch (e: OptimisticLockingFailureException) {
            retryCount++
            OptimisticLockingFailureRetryUtils.log.warn("낙관적 락 충돌 발생 - 재시도 {}회", retryCount)

            if (retryCount >= maxRetryCount) {
                OptimisticLockingFailureRetryUtils.log.error("최대 재시도 초과 - 작업 실패", e)
                throw ErrorCode.INTERNAL_SERVER_ERROR.exception("서버 내부 오류로 인한 작업 실패, 재시도 요청 필요")
            }

            return executeWithRetry(maxRetryCount, operation, retryCount)
        } catch (e: DataIntegrityViolationException) {
            OptimisticLockingFailureRetryUtils.log.error("데이터 무결성 위반 - 작업 실패", e)
            throw ErrorCode.CONFLICT.exception("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요")
        }
    }

    fun interface RetryableOperation<T> {
        @Throws(OptimisticLockingFailureException::class)
        fun execute(): T
    }
}
