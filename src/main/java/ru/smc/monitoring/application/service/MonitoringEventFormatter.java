package ru.smc.monitoring.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.smc.monitoring.application.common.model.request.MonitoringEventRequest;
import ru.smc.monitoring.application.common.model.request.TriggeredUser;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MonitoringEventFormatter {

    private static final String UNKNOWN_USER = "не указан";

    private final TriggeredUserMacroResolver triggeredUserMacroResolver;

    public String format(MonitoringEventRequest request) {
        return """
                %s Новое событие мониторинга

                Уровень:
                  %s

                Канал:
                  %s

                Пользователь:
                  %s

                Сообщение:
                %s

                Время:
                  %s
                """.formatted(
                resolveLevelEmoji(request.level()),
                formatLevel(request.level()),
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

        return "%s (%s)".formatted(name, link.trim());
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

        return "%s %s".formatted(resolveLevelEmoji(level), level.trim().toUpperCase());
    }

    private String indent(String value) {
        if (!StringUtils.hasText(value)) {
            return "  не указано";
        }

        return "  " + value.trim().replace("\n", "\n  ");
    }
}
