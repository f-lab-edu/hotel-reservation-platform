package com.reservation.host.roompricingpolicy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;

public interface JpaRoomPricingPolicyRepository extends JpaRepository<RoomPricingPolicy, Long> {
	Optional<RoomPricingPolicy> findOneByIdAndRoomTypeId(long id, long roomTypeId);
}
