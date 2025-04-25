package com.reservation.host.roomavailability.controller.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NewRoomAvailabilityRequest(
	@NotNull @NotBlank
	LocalDate date,
	@Min(0)
	int availableCount
) {
}
