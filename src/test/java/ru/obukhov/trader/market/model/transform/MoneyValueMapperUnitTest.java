package ru.obukhov.trader.market.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import java.math.BigDecimal;

class MoneyValueMapperUnitTest {

    private final MoneyValueMapper mapper = Mappers.getMapper(MoneyValueMapper.class);

    // region moneyValue to BigDecimal tests

    @Test
    void moneyValueToBigDecimal_whenNull() {
        final BigDecimal bigDecimal = mapper.map((MoneyValue) null);

        Assertions.assertNull(bigDecimal);
    }

    @Test
    void moneyValueToBigDecimal_whenNotNull() {
        final double value = 100;
        final MoneyValue moneyValue = TestData.createMoneyValue(value, Currencies.RUB);

        final BigDecimal bigDecimal = mapper.map(moneyValue);

        AssertUtils.assertEquals(value, bigDecimal);
    }

    // endregion

    // region bigDecimal to MoneyValue tests

    @Test
    void bigDecimalToMoneyValue_whenNull() {
        final MoneyValue moneyValue = mapper.map((BigDecimal) null);

        Assertions.assertNull(moneyValue);
    }

    @Test
    void bigDecimalToMoneyValue_whenNotNull() {
        final BigDecimal bigDecimal = DecimalUtils.setDefaultScale(100.000001);

        final MoneyValue moneyValue = mapper.map(bigDecimal);

        Assertions.assertEquals(100, moneyValue.getUnits());
        Assertions.assertEquals(1000, moneyValue.getNano());
        Assertions.assertEquals(StringUtils.EMPTY, moneyValue.getCurrency());
    }

    // endregion

    // region double to moneyValue tests

    @Test
    void doubleToMoneyValue_whenBigDecimalIsNull() {
        final MoneyValue moneyValue = mapper.map((Double) null);

        Assertions.assertNull(moneyValue);
    }

    @Test
    void doubleToMoneyValue_whenNotNull() {
        final double doubleValue = 100.000001;

        final MoneyValue moneyValue = mapper.map(doubleValue);

        Assertions.assertEquals(100, moneyValue.getUnits());
        Assertions.assertEquals(1000, moneyValue.getNano());
        Assertions.assertEquals(StringUtils.EMPTY, moneyValue.getCurrency());
    }

    // endregion

}