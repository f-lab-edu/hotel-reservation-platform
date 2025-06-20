package com.reservation.domain.payment;

import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.payment.enums.PaymentStatus;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private PaymentStatus status;

	@Column(nullable = false)
	private String paymentUid;

	@Column(nullable = false)
	private Long reservationId;

	@Builder
	public Payment(Integer price, PaymentStatus status, String paymentUid) {
		if (price == null || price < 0) {
			throw ErrorCode.CONFLICT.exception("결제 금액은 0보다 작을 수 없습니다.");
		}
		if (status == null) {
			throw ErrorCode.CONFLICT.exception("결제 상태는 null일 수 없습니다.");
		}
		if (paymentUid == null || paymentUid.isBlank()) {
			throw ErrorCode.CONFLICT.exception("결제 UID는 null 또는 빈 문자열일 수 없습니다.");
		}

		this.price = price;
		this.status = status;
		this.paymentUid = paymentUid;
	}

	public void markCompleted() {
		if (this.status == PaymentStatus.COMPLETED) {
			throw ErrorCode.CONFLICT.exception("이미 결제 완료된 상태입니다.");
		}
		this.status = PaymentStatus.COMPLETED;
	}

	public void markCancelled() {
		if (this.status == PaymentStatus.CANCELLED) {
			throw ErrorCode.CONFLICT.exception("이미 결제 취소된 상태입니다.");
		}
		this.status = PaymentStatus.CANCELLED;
	}
}
