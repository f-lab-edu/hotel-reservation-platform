package com.reservation.customer.reservation.statemachine;

import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import com.reservation.domain.reservation.Reservation;
import com.reservation.domain.reservation.enums.ReservationEvents;
import com.reservation.domain.reservation.enums.ReservationStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReservationStateMachineService {

	private final StateMachineFactory<ReservationStatus, ReservationEvents> factory;

	public void sendEvent(Reservation reservation, ReservationEvents event) {
		StateMachine<ReservationStatus, ReservationEvents> stateMachine =
			factory.getStateMachine(reservation.getId().toString());

		stateMachine.start();

		stateMachine.getStateMachineAccessor()
			.doWithAllRegions(access -> {
				access.resetStateMachine(
					new DefaultStateMachineContext<>(reservation.getStatus(), null, null, null)
				);
			});

		stateMachine.sendEvent(
			MessageBuilder
				.withPayload(event)
				.setHeader("reservation", reservation) // 필요 시 액션에서 접근 가능
				.build()
		);

		stateMachine.stop();
	}

	public void sendEvent(Reservation reservation, Integer iamportAmount, ReservationEvents event) {
		StateMachine<ReservationStatus, ReservationEvents> stateMachine =
			factory.getStateMachine(reservation.getId().toString());

		stateMachine.start();

		stateMachine.getStateMachineAccessor()
			.doWithAllRegions(access -> {
				access.resetStateMachine(
					new DefaultStateMachineContext<>(reservation.getStatus(), null, null, null)
				);
			});

		stateMachine.sendEvent(
			MessageBuilder
				.withPayload(event)
				.setHeader("reservation", reservation) // 필요 시 액션에서 접근 가능
				.setHeader("iamportAmount", iamportAmount) // 필요 시 액션에서 접근 가능
				.build()
		);

		stateMachine.stop();
	}
}
