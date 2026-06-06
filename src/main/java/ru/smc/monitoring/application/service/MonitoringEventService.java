package ru.smc.monitoring.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.smc.monitoring.application.common.exception.UnauthorizedException;
import ru.smc.monitoring.application.common.model.request.MonitoringEventRequest;
import ru.smc.monitoring.application.service.vk.VkNotificationClient;

@Service
@RequiredArgsConstructor
public class MonitoringEventService {

    private final MonitoringEventFormatter monitoringEventFormatter;
    private final VkNotificationClient vkNotificationClient;

    @Value("${api-config.key}")
    private String validApiKey;

    public void processEvent(MonitoringEventRequest request, String apiKey) {
        validateApiKey(apiKey);

        String message = monitoringEventFormatter.format(request);
        vkNotificationClient.sendMessage(message);
    }

    private void validateApiKey(String apiKey) {
        if (!validApiKey.equals(apiKey)) {
            throw new UnauthorizedException("Invalid api-key");
        }
    }
}
