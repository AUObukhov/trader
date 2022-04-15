package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum Currency {

    RUB("RUB"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP"),
    HKD("HKD"),
    CHF("CHF"),
    JPY("JPY"),
    CNY("CNY"),
    TRY("TRY");

    private static final Map<String, Currency> LOOKUP = Stream.of(Currency.values())
            .collect(Collectors.toMap(Currency::getValue, currency -> currency));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static Currency fromValue(String text) {
        return LOOKUP.get(text);
    }

}