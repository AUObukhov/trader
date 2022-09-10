package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;

@Mapper
public interface QuotationMapper {

    default BigDecimal map(final Quotation quotation) {
        return quotation.getUnits() == 0 && quotation.getNano() == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(quotation.getUnits()).add(BigDecimal.valueOf(quotation.getNano(), DecimalUtils.DEFAULT_SCALE));
    }

    default Quotation map(final BigDecimal bigDecimal) {
        return Quotation.newBuilder()
                .setUnits(bigDecimal.longValue())
                .setNano(DecimalUtils.getNano(bigDecimal))
                .build();
    }

    default Quotation map(final double value) {
        return map(BigDecimal.valueOf(value));
    }

}