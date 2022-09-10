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

    // region toBigDecimal tests

    @Test
    void toBigDecimal_whenNull() {
        final BigDecimal bigDecimal = mapper.toBigDecimal(null);

        Assertions.assertNull(bigDecimal);
    }

    @Test
    void toBigDecimal_whenZero() {
        final Quotation quotation = Quotation.newBuilder().setUnits(0).setNano(0).build();

        final BigDecimal bigDecimal = mapper.toBigDecimal(quotation);

        Assertions.assertEquals(BigDecimal.ZERO, bigDecimal);
    }

    @Test
    void toBigDecimal() {
        final Quotation quotation = Quotation.newBuilder().setUnits(100).setNano(100).build();

        final BigDecimal bigDecimal = mapper.toBigDecimal(quotation);

        AssertUtils.assertEquals(100.000000100, bigDecimal);
        AssertUtils.assertEquals(DecimalUtils.DEFAULT_SCALE, bigDecimal.scale());
    }

    // endregion

    // region fromBigDecimal tests

    @Test
    void fromBigDecimal_whenNull() {
        final Quotation quotation = mapper.fromBigDecimal(null);

        Assertions.assertNull(quotation);
    }

    @Test
    void fromBigDecimal_whenZero() {
        final Quotation quotation = mapper.fromBigDecimal(BigDecimal.ZERO);

        Assertions.assertEquals(0, quotation.getUnits());
        Assertions.assertEquals(0, quotation.getNano());
    }

    @Test
    void fromBigDecimal() {
        final BigDecimal bigDecimal = DecimalUtils.setDefaultScale(100.000000100);

        final Quotation quotation = mapper.fromBigDecimal(bigDecimal);

        Assertions.assertEquals(100, quotation.getUnits());
        Assertions.assertEquals(100, quotation.getNano());
    }

    // endregion

    // region fromDouble tests

    @Test
    void fromDouble_whenNull() {
        final Quotation quotation = mapper.fromDouble(null);

        Assertions.assertNull(quotation);
    }

    @Test
    void fromDouble_whenZero() {
        final Quotation quotation = mapper.fromDouble(0.0);

        Assertions.assertEquals(0, quotation.getUnits());
        Assertions.assertEquals(0, quotation.getNano());
    }

    @Test
    void fromDouble() {
        final double doubleValue = 100.000000100;

        final Quotation quotation = mapper.fromDouble(doubleValue);

        Assertions.assertEquals(100, quotation.getUnits());
        Assertions.assertEquals(100, quotation.getNano());
    }

    // endregion

}