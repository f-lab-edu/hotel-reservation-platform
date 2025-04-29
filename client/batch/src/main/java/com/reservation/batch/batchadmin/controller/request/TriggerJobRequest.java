package com.reservation.batch.batchadmin.controller.request;

import java.util.Map;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

public record TriggerJobRequest(
	@NotNull
	String jobName,
	@Nullable
	Map<String, String> jobParametersOrNull
) {
}
