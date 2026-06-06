package ru.smc.monitoring.application.common.exception;

public class VkNotificationException extends RuntimeException {

    public VkNotificationException(String message) {
        super(message);
    }

    public VkNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
