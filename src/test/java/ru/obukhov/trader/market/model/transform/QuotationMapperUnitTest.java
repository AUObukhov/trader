package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;

class QuotationMapperUnitTest {

    private final QuotationMapper mapper = Mappers.getMapper(QuotationMapper.class);

    @Test
    void mapQuotationToBigDecimal_whenZero() {
        final Quotation quotation = Quotation.newBuilder().setUnits(0).setNano(0).build();

        final BigDecimal bigDecimal = mapper.map(quotation);

        Assertions.assertEquals(BigDecimal.ZERO, bigDecimal);
    }

    @Test
    void mapQuotationToBigDecimal() {
        final Quotation quotation = Quotation.newBuilder().setUnits(100).setNano(100).build();

        final BigDecimal bigDecimal = mapper.map(quotation);

        AssertUtils.assertEquals(100.000000100, bigDecimal);
        AssertUtils.assertEquals(DecimalUtils.DEFAULT_SCALE, bigDecimal.scale());
    }

    @Test
    void mapBigDecimalToQuotation() {
        final BigDecimal bigDecimal = DecimalUtils.setDefaultScale(100.000000100);

        final Quotation quotation = mapper.map(bigDecimal);

        Assertions.assertEquals(100, quotation.getUnits());
        Assertions.assertEquals(100, quotation.getNano());
    }

    @Test
    void mapDoubleToQuotation() {
        final double doubleValue = 100.000000100;

        final Quotation quotation = mapper.map(doubleValue);

        Assertions.assertEquals(100, quotation.getUnits());
        Assertions.assertEquals(100, quotation.getNano());
    }

}