package com.reservation.host.roomautoavailabilitypolicy.service;

import org.springframework.stereotype.Service;

import com.reservation.host.roomautoavailabilitypolicy.repository.JpaRoomAutoAvailabilityPolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomAutoAvailabilityPolicyService {
	private final JpaRoomAutoAvailabilityPolicyRepository jpaRoomAutoAvailabilityPolicyRepository;
}
