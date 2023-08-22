package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with methods for calculation of linear weighted moving averages
 */
@Service
public class LinearMovingAverager implements MovingAverager {

    @Override
    public List<Quotation> getAverages(final List<Quotation> values, final int window, final int order) {
        Assert.isTrue(window > 0, "window must be positive");
        Assert.isTrue(order > 0, "order must be positive");

        List<Quotation> averages = new ArrayList<>(values);
        for (int i = 0; i < order; i++) {
            averages = getAveragesInner(averages, window);
        }

        return averages;
    }

    private List<Quotation> getAveragesInner(List<Quotation> values, int window) {
        final List<Quotation> weightedMovingAverages = new ArrayList<>(values.size());
        int normalizedWindow = Math.min(window, values.size());

        int i;
        // filling of first {normalizedWindow} averages
        for (i = 0; i < normalizedWindow; i++) {
            weightedMovingAverages.add(getAverage(values, i, i + 1));
        }
        // filling of the rest averages
        for (; i < values.size(); i++) {
            weightedMovingAverages.add(getAverage(values, i, normalizedWindow));
        }

        return weightedMovingAverages;
    }

    private Quotation getAverage(final List<Quotation> values, final int index, final int window) {
        Quotation sum = QuotationUtils.multiply(values.get(index), window);

        for (int i = index - 1; i > index - window; i--) {
            int weight = window - (index - i);
            sum = QuotationUtils.add(sum, QuotationUtils.multiply(values.get(i), weight));
        }

        final double divisor = window * (window + 1) / 2.0;
        return QuotationUtils.divide(sum, divisor, RoundingMode.HALF_UP);
    }

}