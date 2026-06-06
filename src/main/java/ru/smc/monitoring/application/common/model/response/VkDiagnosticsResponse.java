package ru.smc.monitoring.application.common.model.response;

import java.util.List;

public record VkDiagnosticsResponse(
        Long configuredGroupId,
        Long configuredPeerId,
        boolean groupAvailable,
        String groupName,
        String groupError,
        boolean chatAvailable,
        String chatTitle,
        String chatError,
        List<VkConversationDiagnostic> conversations,
        String conversationsError
) {
}
