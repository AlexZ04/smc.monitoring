package ru.smc.monitoring.application.common.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record MonitoringEventRequest(
        @NotBlank String level,
        @NotBlank String channel,
        @Valid TriggeredUser triggeredBy,
        @NotBlank String message,
        String time
) {
}
