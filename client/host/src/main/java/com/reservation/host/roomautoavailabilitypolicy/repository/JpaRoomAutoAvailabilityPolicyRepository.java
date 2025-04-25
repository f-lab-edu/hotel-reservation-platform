package com.reservation.host.roomautoavailabilitypolicy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;

public interface JpaRoomAutoAvailabilityPolicyRepository extends JpaRepository<RoomAutoAvailabilityPolicy, Long> {
	Optional<RoomAutoAvailabilityPolicy> findOneByIdAndRoomId(long roomAutoAvailabilityPolicyId, long roomId);
}
