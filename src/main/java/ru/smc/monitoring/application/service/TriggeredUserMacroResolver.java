package ru.smc.monitoring.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.smc.monitoring.application.common.model.vk.VkUserProfile;
import ru.smc.monitoring.application.service.vk.VkUserClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TriggeredUserMacroResolver {

    private static final Pattern GET_NAME_PATTERN = Pattern.compile("\\{getName\\(\\*?(\\d+)\\*?\\)\\}");
    private static final Pattern GET_LINK_PATTERN = Pattern.compile("\\{getLink\\(\\*?(\\d+)\\*?\\)\\}");

    private final VkUserClient vkUserClient;

    public String resolveName(String value) {
        return resolve(value, GET_NAME_PATTERN, true);
    }

    public String resolveLink(String value) {
        return resolve(value, GET_LINK_PATTERN, false);
    }

    private String resolve(String value, Pattern pattern, boolean name) {
        if (value == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            Long userId = Long.parseLong(matcher.group(1));
            VkUserProfile profile = vkUserClient.getUserProfile(userId);
            matcher.appendReplacement(result, Matcher.quoteReplacement(name ? profile.name() : profile.link()));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
