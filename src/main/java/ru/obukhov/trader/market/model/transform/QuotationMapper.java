package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;

@Mapper
public interface QuotationMapper {

    default BigDecimal toBigDecimal(final Quotation quotation) {
        return quotation == null
                ? null
                : DecimalUtils.createBigDecimal(quotation.getUnits(), quotation.getNano());
    }

    default Quotation fromBigDecimal(final BigDecimal bigDecimal) {
        return bigDecimal == null
                ? null
                : Quotation.newBuilder().setUnits(bigDecimal.longValue()).setNano(DecimalUtils.getNano(bigDecimal)).build();
    }

    default Quotation fromDouble(final Double value) {
        return value == null
                ? null
                : fromBigDecimal(DecimalUtils.setDefaultScale(value));
    }

}