package ru.obukhov.trader.market.model;

import java.util.List;

public record TradingSchedule(Exchange exchange, List<TradingDay> days) {
}