package com.reservation.customer.payment.service.dto;

import com.siot.IamportRestClient.response.Payment;

public record IamportPayment(
	Payment payment
) {
}
