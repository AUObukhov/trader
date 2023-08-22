package ru.obukhov.trader.common.model.transform;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

/**
 * Class for parsing Quotation from properties
 */
public class DoubleToQuotationConverter implements Converter<Double, Quotation> {

    @Override
    public Quotation convert(@NotNull final Double source) {
        return QuotationUtils.newQuotation(source);
    }

}