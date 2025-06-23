package com.reservation.customer.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.payment.Payment;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
	Optional<Payment> findByPaymentUid(String paymentUid);

	Optional<Payment> findByReservationId(Long reservationId);
}
