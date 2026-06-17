package ru.smc.monitoring.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.smc.monitoring.application.common.model.request.MonitoringEventRequest;
import ru.smc.monitoring.application.common.model.request.TriggeredUser;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MonitoringEventFormatter {

    private static final String UNKNOWN_USER = "не указан";
    private static final Pattern VK_LINK_PATTERN = Pattern.compile("^(?:https?://)?(?:m\\.)?vk\\.com/([^/?#]+).*$");

    private final TriggeredUserMacroResolver triggeredUserMacroResolver;

    public String format(MonitoringEventRequest request) {
        return """
                %s Новое событие с уровнем %s %s

                Канал:  %s

                Пользователь: %s
                
                Сообщение:
                %s

                Время: %s
                """.formatted(
                resolveLevelEmoji(request.level()),
                formatLevel(request.level()),
                resolveLevelEmoji(request.level()),
                request.channel(),
                formatTriggeredBy(request.triggeredBy()),
                indent(triggeredUserMacroResolver.resolveMacros(request.message())),
                formatTime(request.time())
        ).stripTrailing();
    }

    private String formatTriggeredBy(TriggeredUser triggeredBy) {
        if (triggeredBy == null) {
            return UNKNOWN_USER;
        }

        String name = triggeredUserMacroResolver.resolveName(triggeredBy.name());
        name = StringUtils.hasText(name) ? name.trim() : UNKNOWN_USER;

        String link = triggeredUserMacroResolver.resolveLink(triggeredBy.link());
        if (!StringUtils.hasText(link)) {
            return name;
        }

        return formatVkLink(link, name);
    }

    private String formatTime(String time) {
        if (StringUtils.hasText(time)) {
            return time.trim();
        }

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now());
    }

    private String resolveLevelEmoji(String level) {
        if (!StringUtils.hasText(level)) {
            return "⚪";
        }

        return switch (level.trim().toUpperCase()) {
            case "TRACE", "DEBUG" -> "🔵";
            case "INFO" -> "🟢";
            case "WARN", "WARNING" -> "🟡";
            case "ERROR" -> "🔴";
            case "CRITICAL", "FATAL" -> "🚨";
            default -> "⚪";
        };
    }

    private String formatLevel(String level) {
        if (!StringUtils.hasText(level)) {
            return "UNKNOWN";
        }

        return level.trim().toUpperCase();
    }

    private String indent(String value) {
        if (!StringUtils.hasText(value)) {
            return "  не указано";
        }

        return "  " + value.trim().replace("\n", "\n  ");
    }

    private String formatVkLink(String link, String text) {
        String target = normalizeVkLinkTarget(link);
        if (!StringUtils.hasText(target)) {
            return "%s (%s)".formatted(text, link.trim());
        }

        return "[%s | %s]".formatted(target, text);
    }

    private String normalizeVkLinkTarget(String link) {
        String trimmedLink = link.trim();
        Matcher matcher = VK_LINK_PATTERN.matcher(trimmedLink);
        if (matcher.matches()) {
            return "https://vk.com/%s".formatted(matcher.group(1));
        }

        if (trimmedLink.matches("[A-Za-z0-9_.]+")) {
            return "https://vk.com/%s".formatted(trimmedLink);
        }

        return null;
    }
}
