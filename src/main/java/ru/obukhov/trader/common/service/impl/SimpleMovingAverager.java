package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimpleMovingAverager implements MovingAverager {

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

    private List<BigDecimal> getAveragesInner(final List<BigDecimal> values, final int window) {
        final int size = values.size();

        // filling of first {window} averages
        final List<BigDecimal> movingAverages = new ArrayList<>(size);
        final int count = Math.min(window, size);
        for (int i = 0; i < count; i++) {
            BigDecimal sum = DecimalUtils.ZERO;
            for (int j = 0; j <= i; j++) {
                final BigDecimal value = values.get(j);
                sum = sum.add(value);
            }

            movingAverages.add(DecimalUtils.divide(sum, i + 1.0));
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