package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.service.interfaces.MovingAverager;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class with methods for calculation of linear weighted moving averages
 */
@Service
public class LinearMovingAverager implements MovingAverager {

    @Override
    public <T> List<BigDecimal> getAverages(
            final List<T> elements,
            final Function<T, BigDecimal> valueExtractor,
            final int window,
            final int order
    ) {
        final List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getAverages(values, window, order);
    }

    @Override
    public List<BigDecimal> getAverages(
            final List<BigDecimal> values,
            final int window,
            final int order
    ) {
        Assert.isTrue(order > 0, "order must be positive");

        List<BigDecimal> averages = new ArrayList<>(values);
        for (int i = 0; i < order; i++) {
            averages = getAverages(averages, window);
        }

        return averages;
    }

    @Override
    public List<BigDecimal> getAverages(final List<BigDecimal> values, final int window) {
        Assert.isTrue(window > 0, "window must be positive");

        final List<BigDecimal> weightedMovingAverages = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            weightedMovingAverages.add(getLinearWeightedMovingAverage(values, i, window));
        }

        return weightedMovingAverages;
    }

    private static BigDecimal getLinearWeightedMovingAverage(
            final List<BigDecimal> values,
            final int index,
            final int window
    ) {
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