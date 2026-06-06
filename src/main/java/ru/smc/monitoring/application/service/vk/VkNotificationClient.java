package ru.smc.monitoring.application.service.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Conversation;
import com.vk.api.sdk.objects.groups.GroupFull;
import com.vk.api.sdk.objects.groups.responses.GetByIdObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.smc.monitoring.application.common.exception.VkNotificationException;
import ru.smc.monitoring.application.common.model.response.VkConversationDiagnostic;
import ru.smc.monitoring.application.common.model.response.VkDiagnosticsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class VkNotificationClient {

    private final VkApiClient vkApiClient = createVkApiClient();

    @Value("${vk.group-id}")
    private Long groupId;

    @Value("${vk.bot.token}")
    private String token;

    @Value("${vk.monitoring.peer-id}")
    private Long peerId;

    public void sendMessage(String message) {
        GroupActor actor = new GroupActor(groupId, token);
        String destination = resolveDestination(actor);
        log.info("Sending VK monitoring alert to {}", destination);

        try {
            vkApiClient.messages()
                    .sendDeprecated(actor)
                    .peerId(peerId)
                    .message(message)
                    .randomId(ThreadLocalRandom.current().nextInt())
                    .execute();
        } catch (ApiException exception) {
            throw new VkNotificationException(
                    "VK message sending failed: code=%s, message=%s".formatted(
                            exception.getCode(),
                            exception.getDescription()
                    ),
                    exception
            );
        } catch (ClientException exception) {
            throw new VkNotificationException("VK message sending failed: " + exception.getMessage(), exception);
        }
    }

    public VkDiagnosticsResponse getDiagnostics() {
        GroupActor actor = new GroupActor(groupId, token);
        GroupDiagnostic groupDiagnostic = resolveGroup(actor);
        ChatDiagnostic chatDiagnostic = resolveChat(actor);
        ConversationsDiagnostic conversationsDiagnostic = resolveConversations(actor);

        return new VkDiagnosticsResponse(
                groupId,
                peerId,
                groupDiagnostic.available(),
                groupDiagnostic.name(),
                groupDiagnostic.error(),
                chatDiagnostic.available(),
                chatDiagnostic.title(),
                chatDiagnostic.error(),
                conversationsDiagnostic.conversations(),
                conversationsDiagnostic.error()
        );
    }

    private String resolveDestination(GroupActor actor) {
        ChatDiagnostic chatDiagnostic = resolveChat(actor);
        return "peerId=%s, title=%s".formatted(peerId, chatDiagnostic.title());
    }

    private GroupDiagnostic resolveGroup(GroupActor actor) {
        try {
            GetByIdObjectResponse response = vkApiClient.groups()
                    .getByIdObject(actor)
                    .groupId(String.valueOf(groupId))
                    .execute();
            List<GroupFull> groups = response.getGroups();

            if (groups == null || groups.isEmpty()) {
                return new GroupDiagnostic(false, null, "VK returned empty group response");
            }

            return new GroupDiagnostic(true, groups.getFirst().getName(), null);
        } catch (ApiException | ClientException exception) {
            return new GroupDiagnostic(false, null, exception.getMessage());
        }
    }

    private ChatDiagnostic resolveChat(GroupActor actor) {
        try {
            List<Conversation> conversations = vkApiClient.messages()
                    .getConversationsById(actor)
                    .peerIds(peerId)
                    .execute()
                    .getItems();

            if (conversations == null || conversations.isEmpty()) {
                return new ChatDiagnostic(false, "unknown", "VK returned empty conversation response");
            }

            Conversation conversation = conversations.getFirst();
            String title = conversation.getChatSettings() == null
                    ? "unknown"
                    : conversation.getChatSettings().getTitle();

            return new ChatDiagnostic(true, title == null ? "unknown" : title, null);
        } catch (ApiException | ClientException exception) {
            log.warn(
                    "Failed to resolve VK monitoring chat title for peerId={}: {}",
                    peerId,
                    exception.getMessage()
            );

            return new ChatDiagnostic(false, "unknown", exception.getMessage());
        }
    }

    private ConversationsDiagnostic resolveConversations(GroupActor actor) {
        try {
            List<Conversation> conversations = vkApiClient.messages()
                    .getConversations(actor)
                    .count(20)
                    .execute()
                    .getItems()
                    .stream()
                    .map(item -> item.getConversation())
                    .toList();

            List<VkConversationDiagnostic> diagnostics = new ArrayList<>();
            for (Conversation conversation : conversations) {
                diagnostics.add(new VkConversationDiagnostic(
                        conversation.getPeer() == null ? null : conversation.getPeer().getId(),
                        resolveConversationTitle(conversation),
                        conversation.getPeer() == null || conversation.getPeer().getType() == null
                                ? "unknown"
                                : conversation.getPeer().getType().getValue()
                ));
            }

            return new ConversationsDiagnostic(diagnostics, null);
        } catch (ApiException | ClientException exception) {
            return new ConversationsDiagnostic(List.of(), exception.getMessage());
        }
    }

    private String resolveConversationTitle(Conversation conversation) {
        if (conversation.getChatSettings() != null && conversation.getChatSettings().getTitle() != null) {
            return conversation.getChatSettings().getTitle();
        }

        return "unknown";
    }

    private VkApiClient createVkApiClient() {
        TransportClient transportClient = new HttpTransportClient();

        return new VkApiClient(transportClient);
    }

    private record GroupDiagnostic(boolean available, String name, String error) {
    }

    private record ChatDiagnostic(boolean available, String title, String error) {
    }

    private record ConversationsDiagnostic(List<VkConversationDiagnostic> conversations, String error) {
    }
}
