package ru.obukhov.trader.market.model;

import org.apache.commons.lang3.StringUtils;

public enum Sector {
    IT,
    CONSUMER,
    HEALTH_CARE,
    FINANCIAL,
    INDUSTRIALS,
    MATERIALS,
    ECOMATERIALS,
    ENERGY,
    GREEN_ENERGY,
    REAL_ESTATE,
    GREEN_BUILDINGS,
    UTILITIES,
    TELECOM,
    ELECTROCARS,
    OTHER,
    UNKNOWN;

    public static Sector valueOfIgnoreCase(final String name) {
        return StringUtils.isEmpty(name) ? Sector.UNKNOWN : Sector.valueOf(name.toUpperCase());
    }
}