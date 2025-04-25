package com.reservation.host.roomautoavailabilitypolicy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomautoavailabilitypolicy.RoomAutoAvailabilityPolicy;

public interface JpaRoomAutoAvailabilityPolicyRepository extends JpaRepository<RoomAutoAvailabilityPolicy, Long> {
}
