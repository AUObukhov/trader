package ru.obukhov.trader.market.model;

import java.math.BigDecimal;
import java.util.Map;

public record SetCapitalization(Map<String, BigDecimal> sharesCapitalizations, BigDecimal totalCapitalization) {
}