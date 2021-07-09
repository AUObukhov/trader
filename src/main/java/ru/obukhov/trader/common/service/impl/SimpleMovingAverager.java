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

@Service
public class SimpleMovingAverager implements MovingAverager {

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

        List<BigDecimal> averages = new ArrayList<>(values);
        for (int i = 0; i < order; i++) {
            averages = getAverages(averages, window);
        }

        return averages;
    }

    @Override
    public <T> List<BigDecimal> getAverages(
            final List<T> elements,
            final Function<T, BigDecimal> valueExtractor,
            final int window
    ) {
        final List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getAverages(values, window);
    }

    @Override
    public List<BigDecimal> getAverages(final List<BigDecimal> values, final int window) {
        Assert.isTrue(window > 0, "window must be positive");

        final int size = values.size();

        // filling of first {window} averages
        final List<BigDecimal> movingAverages = new ArrayList<>(size);
        final int count = Math.min(window, size);
        for (int i = 0; i < count; i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = 0; j <= i; j++) {
                final BigDecimal value = values.get(j);
                sum = sum.add(value);
            }

            movingAverages.add(DecimalUtils.divide(sum, i + 1));
        }

        // filling of the rest averages
        for (int i = window; i < size; i++) {
            final BigDecimal excludedValue = DecimalUtils.divide(values.get(i - window), window);
            final BigDecimal addedValue = DecimalUtils.divide(values.get(i), window);
            final BigDecimal currentAverage = movingAverages.get(i - 1).subtract(excludedValue).add(addedValue);
            movingAverages.add(currentAverage);
        }

        return movingAverages;
    }

}