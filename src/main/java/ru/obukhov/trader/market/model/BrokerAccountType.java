package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum BrokerAccountType {

    TINKOFF("Tinkoff"),
    TINKOFF_IIS("TinkoffIis");

    private static final Map<String, BrokerAccountType> LOOKUP = Stream.of(BrokerAccountType.values())
            .collect(Collectors.toMap(BrokerAccountType::getValue, brokerAccountType -> brokerAccountType));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static BrokerAccountType fromValue(String text) {
        return LOOKUP.get(text);
    }

}