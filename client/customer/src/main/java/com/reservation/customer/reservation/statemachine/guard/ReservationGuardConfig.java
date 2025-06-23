package com.reservation.customer.reservation.statemachine.guard;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.guard.Guard;

import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationEvents;
import com.reservation.domain.reservation.enums.ReservationStatus;

@Configuration
public class ReservationGuardConfig {

	@Bean
	public Guard<ReservationStatus, ReservationEvents> isPaymentAmountValidGuard() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			Integer iamportPrice = (Integer)context.getMessageHeader("paidAmount");

			return Objects.equals(reservation.getTotalPrice(), iamportPrice);
		};
	}

	@Bean
	public Guard<ReservationStatus, ReservationEvents> isPaymentAmountInvalidGuard() {
		return context -> {
			Reservation reservation = (Reservation)context.getMessageHeader("reservation");
			Integer iamportPrice = (Integer)context.getMessageHeader("paidAmount");
            
			return !Objects.equals(reservation.getTotalPrice(), iamportPrice);
		};
	}
}
