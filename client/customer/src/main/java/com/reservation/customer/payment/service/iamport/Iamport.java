package com.reservation.customer.payment.service.iamport;

import org.springframework.stereotype.Component;

import com.reservation.support.exception.ErrorCode;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.Payment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class Iamport {
	private final IamportClient client;

	// iamport 결제 정보 조회
	public Payment paymentByImpUid(String paymentUid) {
		try {
			return client.paymentByImpUid(paymentUid).getResponse();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.CONFLICT.exception("결제 정보 조회 실패");
		}
	}

	public Payment cancelPaymentByImpUid(CancelData cancelData) {
		try {
			return client.cancelPaymentByImpUid(cancelData).getResponse();
		} catch (Exception e) {
			log.error(e.getMessage());
			throw ErrorCode.CONFLICT.exception("결제금액 위변조 의심 - 결제 취소 실패");
		}
	}
}
