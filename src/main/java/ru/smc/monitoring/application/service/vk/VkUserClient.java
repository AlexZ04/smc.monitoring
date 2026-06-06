package ru.smc.monitoring.application.service.vk;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.users.Fields;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.smc.monitoring.application.common.model.vk.VkUserProfile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class VkUserClient {

    private final VkApiClient vkApiClient = createVkApiClient();
    private final Map<Long, VkUserProfile> cache = new ConcurrentHashMap<>();

    @Value("${vk.group-id}")
    private Long groupId;

    @Value("${vk.bot.token}")
    private String token;

    public VkUserProfile getUserProfile(Long userId) {
        return cache.computeIfAbsent(userId, this::loadUserProfile);
    }

    private VkUserProfile loadUserProfile(Long userId) {
        try {
            List<GetResponse> users = vkApiClient.users()
                    .get(new GroupActor(groupId, token))
                    .userIds(String.valueOf(userId))
                    .fields(Fields.SCREEN_NAME)
                    .execute();

            if (users == null || users.isEmpty()) {
                return fallbackProfile(userId);
            }

            GetResponse user = users.getFirst();
            String name = "%s %s".formatted(user.getFirstName(), user.getLastName()).trim();
            String screenName = user.getScreenName();

            return new VkUserProfile(
                    StringUtils.hasText(name) ? name : "id%s".formatted(userId),
                    StringUtils.hasText(screenName)
                            ? "https://vk.com/%s".formatted(screenName)
                            : makeIdLink(userId)
            );
        } catch (ApiException | ClientException exception) {
            log.warn("Failed to resolve VK user profile for userId={}: {}", userId, exception.getMessage());

            return fallbackProfile(userId);
        }
    }

    private VkUserProfile fallbackProfile(Long userId) {
        return new VkUserProfile("id%s".formatted(userId), makeIdLink(userId));
    }

    private String makeIdLink(Long userId) {
        return "https://vk.com/id%s".formatted(userId);
    }

    private VkApiClient createVkApiClient() {
        TransportClient transportClient = new HttpTransportClient();

        return new VkApiClient(transportClient);
    }
}
