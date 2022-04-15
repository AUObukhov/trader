package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum OperationStatus {

    DONE("Done"),
    DECLINE("Decline"),
    PROGRESS("Progress");

    private static final Map<String, OperationStatus> LOOKUP = Stream.of(OperationStatus.values())
            .collect(Collectors.toMap(OperationStatus::getValue, operationStatus -> operationStatus));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static OperationStatus fromValue(String text) {
        return LOOKUP.get(text);
    }

}