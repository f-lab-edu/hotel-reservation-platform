package com.reservation.host.room.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.room.Room;

public interface JpaRoomRepository extends JpaRepository<Room, Long> {
	boolean existsByNameAndAccommodationId(String name, Long accommodationId);

	boolean existsByIdAndAccommodationId(Long id, Long accommodationId);

	Optional<Room> findOneByNameAndAccommodationId(String name, Long accommodationId);

	Optional<Room> findOneByIdAndAccommodationId(Long id, Long accommodationId);
}
