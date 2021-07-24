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
 * Class with methods for calculation of exponentially weighted moving averages
 */
@Service
public class ExponentialMovingAverager implements MovingAverager {

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
    public List<BigDecimal> getAverages(final List<BigDecimal> values, final int window, final int order) {
        Assert.isTrue(order > 0, "order must be positive");
        Assert.isTrue(window > 0, "window must be positive");

        List<BigDecimal> averages = new ArrayList<>(values);
        if (averages.isEmpty()) {
            return averages;
        }
        final double weightDecrease = 2.0 / (window + 1);
        final double revertedWeightDecrease = 1.0 - weightDecrease;

        for (int i = 0; i < order; i++) {
            updateExponentialMovingAverages(averages, weightDecrease, revertedWeightDecrease);
        }

        return averages.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    @Override
    public <T> List<BigDecimal> getAverages(
            final List<T> elements,
            final Function<T, BigDecimal> valueExtractor,
            final int window
    ) {
        return getAverages(elements, valueExtractor, window, 1);
    }

    @Override
    public List<BigDecimal> getAverages(final List<BigDecimal> values, final int window) {
        return getAverages(values, window, 1);
    }

    private static void updateExponentialMovingAverages(
            final List<BigDecimal> averages,
            final double weightDecrease,
            final double revertedWeightDecrease
    ) {
        BigDecimal average = averages.get(0);
        final int size = averages.size();
        for (int i = 1; i < size; i++) {
            average = updateExponentialMovingAverage(averages, average, i, weightDecrease, revertedWeightDecrease);
        }
    }

    private static BigDecimal updateExponentialMovingAverage(
            final List<BigDecimal> averages,
            final BigDecimal previousAverage,
            final int index,
            final double weightDecrease,
            final double revertedWeightDecrease
    ) {
        final BigDecimal average = DecimalUtils.multiply(averages.get(index), weightDecrease)
                .add(DecimalUtils.multiply(previousAverage, revertedWeightDecrease));
        averages.set(index, average);
        return average;
    }

}