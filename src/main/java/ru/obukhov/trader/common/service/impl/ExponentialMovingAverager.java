package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Class with methods for calculation of exponentially weighted moving averages
 */
@Service
public class ExponentialMovingAverager implements MovingAverager {

    @Override
    public List<Quotation> getAverages(final List<Quotation> values, final int window, final int order) {
        Assert.isTrue(order > 0, "order must be positive");
        Assert.isTrue(window > 0, "window must be positive");

        List<Quotation> averages = new ArrayList<>(values);
        if (averages.isEmpty()) {
            return averages;
        }
        final Quotation weightDecrease = QuotationUtils.newQuotation(2.0 / (window + 1));
        final Quotation revertedWeightDecrease = QuotationUtils.subtract(1L, weightDecrease);

        for (int i = 0; i < order; i++) {
            updateAverages(averages, weightDecrease, revertedWeightDecrease);
        }

        return averages;
    }

    private void updateAverages(final List<Quotation> averages, final Quotation weightDecrease, final Quotation revertedWeightDecrease) {
        Quotation average = averages.get(0);
        final int size = averages.size();
        for (int i = 1; i < size; i++) {
            average = updateAverage(averages, average, i, weightDecrease, revertedWeightDecrease);
        }
    }

    private Quotation updateAverage(
            final List<Quotation> averages,
            final Quotation previousAverage,
            final int index,
            final Quotation weightDecrease,
            final Quotation revertedWeightDecrease
    ) {
        final Quotation average = QuotationUtils.add(
                QuotationUtils.multiply(averages.get(index), weightDecrease),
                QuotationUtils.multiply(previousAverage, revertedWeightDecrease)
        );
        averages.set(index, average);
        return average;
    }

}