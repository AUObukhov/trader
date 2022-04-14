package ru.obukhov.trader.market.model;

import java.math.BigDecimal;

public record MarketInstrument(
        String figi,
        String ticker,
        String isin,
        BigDecimal minPriceIncrement,
        Integer lot,
        Integer minQuantity,
        Currency currency,
        String name,
        InstrumentType type
) {
}