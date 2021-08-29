package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with methods for calculation of linear weighted moving averages
 */
@Service
public class LinearMovingAverager extends MovingAverager {

    @Override
    public List<BigDecimal> getAverages(final List<BigDecimal> values, final int window, final int order) {
        Assert.isTrue(window > 0, "window must be positive");
        Assert.isTrue(order > 0, "order must be positive");

        List<BigDecimal> averages = new ArrayList<>(values);
        for (int i = 0; i < order; i++) {
            averages = getAveragesInner(averages, window);
        }

        return averages;
    }

    private List<BigDecimal> getAveragesInner(List<BigDecimal> values, int window) {
        final List<BigDecimal> weightedMovingAverages = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            weightedMovingAverages.add(getAverage(values, i, window));
        }

        return weightedMovingAverages;
    }

    private BigDecimal getAverage(final List<BigDecimal> values, final int index, final int window) {
        final int maxPeriod = index + 1;
        final int normalizedPeriod = Math.min(window, maxPeriod);
        BigDecimal sum = DecimalUtils.multiply(values.get(index), normalizedPeriod);
        BigDecimal weightsSum = BigDecimal.valueOf(normalizedPeriod);

        for (int i = index - 1; i > index - normalizedPeriod; i--) {
            int weight = normalizedPeriod - (index - i);
            sum = sum.add(DecimalUtils.multiply(values.get(i), weight));
            weightsSum = weightsSum.add(BigDecimal.valueOf(weight));
        }

        final double divisor = normalizedPeriod * (normalizedPeriod + 1) / 2.0;
        return DecimalUtils.divide(sum, divisor);
    }

}