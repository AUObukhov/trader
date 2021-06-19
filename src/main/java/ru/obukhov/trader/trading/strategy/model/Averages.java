package ru.obukhov.trader.trading.strategy.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * POJO with two averages lists for golden cross strategies
 */
@Getter
@AllArgsConstructor
public class Averages {
    private final List<BigDecimal> shortAverages;
    private final List<BigDecimal> longAverages;
}