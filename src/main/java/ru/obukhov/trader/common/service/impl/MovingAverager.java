package ru.obukhov.trader.common.service.impl;

import java.math.BigDecimal;
import java.util.List;

/**
 * @see <a href=https://www.investopedia.com/terms/m/movingaverage.asp">investopedia</a>
 */
public interface MovingAverager {

    default List<BigDecimal> getAverages(final List<BigDecimal> values, final int window) {
        return getAverages(values, window, 1);
    }

    List<BigDecimal> getAverages(final List<BigDecimal> values, final int window, final int order);

}