package com.reservation.host.roompricingpolicy.service;

import org.springframework.stereotype.Service;

import com.reservation.host.roompricingpolicy.repository.JpaRoomPricingPolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomPricingPolicyService {
	private final JpaRoomPricingPolicyRepository jpaRoomPricingPolicyRepository;
}
