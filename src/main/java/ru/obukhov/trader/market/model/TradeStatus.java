package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum TradeStatus {

    NORMALTRADING("NormalTrading"),
    NOTAVAILABLEFORTRADING("NotAvailableForTrading");

    private static final Map<String, TradeStatus> LOOKUP = Stream.of(TradeStatus.values())
            .collect(Collectors.toMap(TradeStatus::getValue, tradeStatus -> tradeStatus));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static TradeStatus fromValue(String text) {
        return LOOKUP.get(text);
    }

}