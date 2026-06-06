package ru.smc.monitoring.application.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.smc.monitoring.application.common.model.request.MonitoringEventRequest;
import ru.smc.monitoring.application.common.model.request.TriggeredUser;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MonitoringEventFormatter {

    private static final String UNKNOWN_USER = "не указан";

    public String format(MonitoringEventRequest request) {
        return """
                Новое событие с уровнем %s в канале %s!
                Пользователь, стриггеревший событие: %s.
                Сообщение: %s
                Время: %s
                """.formatted(
                request.level(),
                request.channel(),
                formatTriggeredBy(request.triggeredBy()),
                request.message(),
                formatTime(request.time())
        ).stripTrailing();
    }

    private String formatTriggeredBy(TriggeredUser triggeredBy) {
        if (triggeredBy == null) {
            return UNKNOWN_USER;
        }

        String name = StringUtils.hasText(triggeredBy.name()) ? triggeredBy.name().trim() : UNKNOWN_USER;
        if (!StringUtils.hasText(triggeredBy.link())) {
            return name;
        }

        return "%s (%s)".formatted(name, triggeredBy.link().trim());
    }

    private String formatTime(String time) {
        if (StringUtils.hasText(time)) {
            return time.trim();
        }

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now());
    }
}
