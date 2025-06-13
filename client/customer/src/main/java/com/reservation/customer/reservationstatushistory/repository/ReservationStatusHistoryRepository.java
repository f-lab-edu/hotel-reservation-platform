package com.reservation.customer.reservationstatushistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.reservation.domain.reservationstatushistory.ReservationStatusHistory;

@Repository
public interface ReservationStatusHistoryRepository extends JpaRepository<ReservationStatusHistory, Long> {
}
