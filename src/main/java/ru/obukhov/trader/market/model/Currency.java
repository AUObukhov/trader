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

    /**
     * @return corresponding instance of {@link java.util.Currency}
     */
    public java.util.Currency getJavaCurrency() {
        return java.util.Currency.getInstance(name());
    }

    public static Currency valueOfIgnoreCase(final String name) {
        return Currency.valueOf(name.toUpperCase());
    }
}