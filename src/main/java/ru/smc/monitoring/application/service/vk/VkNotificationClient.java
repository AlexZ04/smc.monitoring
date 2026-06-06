package ru.smc.monitoring.application.service.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.smc.monitoring.application.common.exception.VkNotificationException;

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
        log.info("Sending VK monitoring alert to peerId={}", peerId);

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

    private VkApiClient createVkApiClient() {
        TransportClient transportClient = new HttpTransportClient();

        return new VkApiClient(transportClient);
    }
}
