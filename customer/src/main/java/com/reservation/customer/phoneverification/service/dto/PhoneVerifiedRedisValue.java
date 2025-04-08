package com.reservation.customer.phoneverification.service.dto;

import java.util.Set;

public record PhoneVerifiedRedisValue(Set<Long> agreedTermsIds) {
}
