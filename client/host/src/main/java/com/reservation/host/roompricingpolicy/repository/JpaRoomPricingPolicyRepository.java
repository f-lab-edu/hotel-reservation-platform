package com.reservation.host.roompricingpolicy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;

public interface JpaRoomPricingPolicyRepository extends JpaRepository<RoomPricingPolicy, Long> {
}
