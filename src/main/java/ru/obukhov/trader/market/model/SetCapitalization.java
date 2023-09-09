package ru.obukhov.trader.market.model;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Capitalization data of some set of shares, e.g. portfolio index, etc.
 *
 * @param sharesCapitalizations capitalizations of the securities by FIGI
 * @param totalCapitalization   total capitalization of the set
 */
public record SetCapitalization(Map<String, BigDecimal> sharesCapitalizations, BigDecimal totalCapitalization) {
}