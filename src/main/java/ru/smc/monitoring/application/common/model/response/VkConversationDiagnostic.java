package ru.smc.monitoring.application.common.model.response;

public record VkConversationDiagnostic(
        Long peerId,
        String title,
        String type
) {
}
