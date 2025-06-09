package com.reservation.batch.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.reservation.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {
}
