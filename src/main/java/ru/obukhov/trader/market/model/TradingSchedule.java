package ru.obukhov.trader.market.model;

import java.util.List;

public record TradingSchedule(String exchange, List<TradingDay> days) {
    public TradingSchedule(final String exchange, final TradingDay... days) {
        this(exchange, List.of(days));
    }
}