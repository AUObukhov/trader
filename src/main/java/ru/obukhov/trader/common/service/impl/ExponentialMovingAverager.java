package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with methods for calculation of exponentially weighted moving averages
 */
@Service
public class ExponentialMovingAverager extends MovingAverager {

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
            updateAverages(averages, weightDecrease, revertedWeightDecrease);
        }

        return averages.stream().map(DecimalUtils::setDefaultScale).toList();
    }

    private void updateAverages(final List<BigDecimal> averages, final double weightDecrease, final double revertedWeightDecrease) {
        BigDecimal average = averages.get(0);
        final int size = averages.size();
        for (int i = 1; i < size; i++) {
            average = updateAverage(averages, average, i, weightDecrease, revertedWeightDecrease);
        }
    }

    private BigDecimal updateAverage(
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