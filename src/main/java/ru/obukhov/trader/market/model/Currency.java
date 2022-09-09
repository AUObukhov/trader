package ru.obukhov.trader.market.model;

import org.apache.commons.lang3.StringUtils;

public enum Currency {
    RUB,
    USD,
    EUR,
    GBP,
    HKD,
    CHF,
    JPY,
    CNY,
    TRY,
    ILS,
    CAD,
    DKK,
    SEK,
    SGD,
    NOK,
    UNKNOWN;

    public static Currency valueOfIgnoreCase(final String name) {
        return StringUtils.isEmpty(name) ? Currency.UNKNOWN : Currency.valueOf(name.toUpperCase());
    }
}