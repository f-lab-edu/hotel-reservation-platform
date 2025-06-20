package com.reservation.customer.reservation.statemachine;

import static com.reservation.domain.reservation.enums.ReservationEvents.*;
import static com.reservation.domain.reservation.enums.ReservationStatus.*;
import static com.reservation.domain.reservation.enums.ReservationStatus.PG_CANCEL_FAIL;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import com.reservation.domain.reservation.enums.ReservationEvents;
import com.reservation.domain.reservation.enums.ReservationStatus;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableStateMachineFactory
// @EnableStateMachine
@RequiredArgsConstructor
public class StateMachineConfig extends StateMachineConfigurerAdapter<ReservationStatus, ReservationEvents> {

	private final Action<ReservationStatus, ReservationEvents> markPaidErrorAction;
	private final Guard<ReservationStatus, ReservationEvents> isPaymentAmountValidGuard;
	private final Action<ReservationStatus, ReservationEvents> markPaidAction;
	private final Guard<ReservationStatus, ReservationEvents> isPaymentAmountInvalidGuard;
	private final Action<ReservationStatus, ReservationEvents> markExpiredAction;
	private final Action<ReservationStatus, ReservationEvents> markConfirmedAction;
	private final Action<ReservationStatus, ReservationEvents> markPaidErrorCanceledAction;
	private final Action<ReservationStatus, ReservationEvents> markPgCancelFailAction;

	@Override
	public void configure(StateMachineStateConfigurer<ReservationStatus, ReservationEvents> states) throws Exception {
		states
			.withStates()
			.initial(ReservationStatus.PAID) // 초기 상태
			.state(ReservationStatus.PAID_ERROR) // 결제 에러 상태
			.state(ReservationStatus.CANCELED) // 예약 취소 상태
			.state(ReservationStatus.CONFIRMED) // 예약 확정 상태
			.state(ReservationStatus.PAID_ERROR_CANCELED) // 결제 에러로 인한 결제 취소 상태
			.state(ReservationStatus.PAID_CANCELED) // 결제 취소 상태
			.state(ReservationStatus.PG_VALIDATE_ERROR) // PG 검증 실패 상태
			.state(ReservationStatus.PG_CANCEL_FAIL); // PG 결제 취소 실패 상태
	}

	@Override
	public void configure(
		StateMachineTransitionConfigurer<ReservationStatus, ReservationEvents> transitions
	) throws Exception {
		transitions
			// PG_VALIDATE_ERROR → 재검증
			.withExternal().source(PG_VALIDATE_ERROR).target(PAID)
			.event(PAYMENT_SUCCESS)
			.guard(isPaymentAmountValidGuard)
			.action(markPaidAction)
			.and()
			.withExternal().source(PG_VALIDATE_ERROR).target(PAID_ERROR)
			.event(PAYMENT_SUCCESS)
			.guard(isPaymentAmountInvalidGuard)
			.action(markPaidErrorAction)

			// PAID → 확정
			.and()
			.withExternal().source(PAID).target(CONFIRMED)
			.event(CHECKIN_PASSED)
			.action(markConfirmedAction)

			// PAID_ERROR → 결제 취소 or 실패
			.and()
			.withExternal().source(PAID_ERROR).target(PAID_ERROR_CANCELED)
			.event(PG_PAID_CANCEL)
			.action(markPaidErrorCanceledAction)
			.and()
			.withExternal().source(PAID_ERROR).target(PG_CANCEL_FAIL)
			.event(ReservationEvents.PG_CANCEL_FAIL)
			.action(markPgCancelFailAction);
	}
}
