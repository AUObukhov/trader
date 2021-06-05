package ru.obukhov.trader.web;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class TraderExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            final MethodArgumentNotValidException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponseMap(exception));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(final Exception exception) {
        log.error("Unknown exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseMap(exception));
    }

    private Map<String, Object> createResponseMap(final MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.toList());

        return createResponseMap("Invalid request", errors);
    }

    private Map<String, Object> createResponseMap(final Exception exception) {
        return createResponseMap(exception.getMessage(), null);
    }

    private Map<String, Object> createResponseMap(final String message, final List<String> errors) {
        final Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        if (CollectionUtils.isNotEmpty(errors)) {
            result.put("errors", errors);
        }
        result.put("time", OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

}