package ru.obukhov.investor.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.obukhov.investor.exception.InvestorException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

@Slf4j
@ControllerAdvice
public class InvestorExceptionHandler {

    @ExceptionHandler(InvestorException.class)
    public ResponseEntity<Map<String, String>> handleInvestorException(InvestorException ex) {
        log.error("Investor exception", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(createResponseMap(ex.getMessage()));
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<Map<String, String>> handleCompletionException(Exception ex) {
        log.error("Unknown tinkoff exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseMap(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("Unknown exception", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createResponseMap(ex.getMessage()));
    }

    private Map<String, String> createResponseMap(String message) {
        Map<String, String> result = new HashMap<>();
        result.put("message", message);
        result.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }
}
