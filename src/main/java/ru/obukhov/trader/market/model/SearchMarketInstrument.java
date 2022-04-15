package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record SearchMarketInstrument(
        String figi,
        String ticker,
        String isin,
        BigDecimal minPriceIncrement,
        Integer lot,
        Currency currency,
        String name,
        InstrumentType type
) {
}