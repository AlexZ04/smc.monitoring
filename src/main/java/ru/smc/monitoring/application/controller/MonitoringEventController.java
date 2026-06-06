package ru.smc.monitoring.application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.smc.monitoring.application.common.model.request.MonitoringEventRequest;
import ru.smc.monitoring.application.common.model.response.MonitoringEventResponse;
import ru.smc.monitoring.application.common.model.response.VkDiagnosticsResponse;
import ru.smc.monitoring.application.service.MonitoringEventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class MonitoringEventController {

    private final MonitoringEventService monitoringEventService;

    @PostMapping("/events")
    public MonitoringEventResponse processEvent(
            @Valid @RequestBody MonitoringEventRequest request,
            @RequestHeader("api-key") String apiKey
    ) {
        monitoringEventService.processEvent(request, apiKey);

        return new MonitoringEventResponse("sent");
    }

    @PostMapping("/vk/diagnostics")
    public VkDiagnosticsResponse getVkDiagnostics(@RequestHeader("api-key") String apiKey) {
        return monitoringEventService.getVkDiagnostics(apiKey);
    }
}
