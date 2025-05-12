package com.reservation.host.roomtype.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.roomtype.RoomType;

public interface JpaRoomTypeRepository extends JpaRepository<RoomType, Long> {
	boolean existsByNameAndAccommodationId(String name, Long accommodationId);

	boolean existsByIdAndAccommodationId(Long id, Long accommodationId);

	Optional<RoomType> findOneByNameAndAccommodationId(String name, Long accommodationId);

	Optional<RoomType> findOneByIdAndAccommodationId(Long id, Long accommodationId);
}
