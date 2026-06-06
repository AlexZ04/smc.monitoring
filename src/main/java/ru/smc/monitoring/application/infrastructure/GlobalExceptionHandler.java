package ru.smc.monitoring.application.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.smc.monitoring.application.common.exception.UnauthorizedException;
import ru.smc.monitoring.application.common.exception.VkNotificationException;
import ru.smc.monitoring.application.common.model.response.ErrorResponse;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> catchValidationException(MethodArgumentNotValidException exception) {
        logError(exception);

        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request body");
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> catchMissingHeaderException(MissingRequestHeaderException exception) {
        logError(exception);

        return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid api-key");
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> catchUnauthorizedException(UnauthorizedException exception) {
        logError(exception);

        return createErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> catchVkNotificationException(VkNotificationException exception) {
        logError(exception);

        return createErrorResponse(HttpStatus.BAD_GATEWAY, exception.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> catchUnknownException(Exception exception) {
        logError(exception);

        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }

    private ResponseEntity<ErrorResponse> createErrorResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(new ErrorResponse(status.value(), message), status);
    }

    private void logError(Exception exception) {
        log.error("Received new error: {}", exception.getMessage(), exception);
    }
}
