package ru.obukhov.trader.market.model;

import java.util.List;

public record TradingSchedule(String exchange, List<TradingDay> days) {
}