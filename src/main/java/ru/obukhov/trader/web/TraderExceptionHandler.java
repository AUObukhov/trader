package ru.obukhov.trader.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.util.DateUtils;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class TraderExceptionHandler {

    @SuppressWarnings("unused")
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(final MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponseMap(exception));
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(final ConstraintViolationException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponseMap(exception));
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParameterException(final MissingServletRequestParameterException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponseMap(exception));
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(final Exception exception) {
        log.error("Unknown exception", exception);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseMap(exception));
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(InstrumentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleException(final InstrumentNotFoundException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponseMap(exception));
    }

    private Map<String, Object> createResponseMap(final MethodArgumentNotValidException exception) {
        final List<String> errors = exception.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .filter(Objects::nonNull)
                .toList();

        return createResponseMap("Invalid request", errors);
    }

    private Map<String, Object> createResponseMap(final ConstraintViolationException exception) {
        final List<String> errors = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
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
        result.put("time", DateUtils.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

}