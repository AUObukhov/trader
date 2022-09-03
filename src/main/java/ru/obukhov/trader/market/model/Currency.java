package ru.obukhov.trader.market.model;

public enum Currency {
    RUB,
    USD,
    EUR,
    GBP,
    HKD,
    CHF,
    JPY,
    CNY,
    TRY;

    public static Currency valueOfIgnoreCase(final String name) {
        return Currency.valueOf(name.toUpperCase());
    }
}