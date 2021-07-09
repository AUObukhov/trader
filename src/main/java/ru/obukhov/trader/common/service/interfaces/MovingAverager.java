package ru.obukhov.trader.common.service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

public interface MovingAverager {

    <T> List<BigDecimal> getAverages(
            final List<T> elements,
            final Function<T, BigDecimal> valueExtractor,
            final int window,
            final int order
    );

    List<BigDecimal> getAverages(final List<BigDecimal> values, final int window, final int order);

    <T> List<BigDecimal> getAverages(
            final List<T> elements,
            final Function<T, BigDecimal> valueExtractor,
            final int window
    );

    List<BigDecimal> getAverages(final List<BigDecimal> values, final int window);

}