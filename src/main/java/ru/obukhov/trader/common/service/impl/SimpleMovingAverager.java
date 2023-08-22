package ru.obukhov.trader.common.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with methods for calculation of simple moving averages
 */
@Service
public class SimpleMovingAverager implements MovingAverager {

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
        final int size = values.size();

        // filling of first {window} averages
        final List<Quotation> movingAverages = new ArrayList<>(size);
        final int count = Math.min(window, size);
        for (int i = 0; i < count; i++) {
            Quotation sum = QuotationUtils.newNormalizedQuotation(0, 0);
            for (int j = 0; j <= i; j++) {
                final Quotation value = values.get(j);
                sum = QuotationUtils.add(sum, value);
            }

            movingAverages.add(QuotationUtils.divide(sum, i + 1L, RoundingMode.HALF_UP));
        }

        // filling of the rest averages
        for (int i = window; i < size; i++) {
            final Quotation excludedValue = QuotationUtils.divide(values.get(i - window), window, RoundingMode.HALF_UP);
            final Quotation addedValue = QuotationUtils.divide(values.get(i), window, RoundingMode.HALF_UP);
            final Quotation currentAverage = QuotationUtils.add(QuotationUtils.subtract(movingAverages.get(i - 1), excludedValue), addedValue);
            movingAverages.add(currentAverage);
        }

        return movingAverages;
    }

}