package com.reservation.batch.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roompricingpolicy.RoomPricingPolicy;

public interface JpaRoomPricingPolicyRepository extends JpaRepository<RoomPricingPolicy, Long> {
	List<RoomPricingPolicy> findByRoomTypeIdIn(List<Long> roomTypeIds);
}
