package ru.obukhov.trader.market.model;

import java.math.BigDecimal;
import java.util.List;

public record MarketInstrumentList(BigDecimal total, List<MarketInstrument> instruments) {
}