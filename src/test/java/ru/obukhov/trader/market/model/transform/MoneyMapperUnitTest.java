package ru.obukhov.trader.market.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Money;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import java.math.BigDecimal;

class MoneyMapperUnitTest {

    private final MoneyMapper mapper = Mappers.getMapper(MoneyMapper.class);

    // region moneyValueToMoney tests

    @Test
    void moneyValueToMoney_whenNull() {
        final Money money = mapper.moneyValueToMoney(null);

        Assertions.assertNull(money);
    }

    @Test
    void moneyValueToMoney_whenNotNull() {
        final double value = 100;
        final Currency currency = Currency.RUB;
        final MoneyValue moneyValue = TestData.createTinkoffMoneyValue(value, currency);

        final Money money = mapper.moneyValueToMoney(moneyValue);

        AssertUtils.assertEquals(value, money.value());
        Assertions.assertEquals(currency, money.currency());
    }

    // endregion

    // region moneyValueToBigDecimal tests

    @Test
    void moneyValueToBigDecimal_whenNull() {
        final BigDecimal bigDecimal = mapper.moneyValueToBigDecimal(null);

        Assertions.assertNull(bigDecimal);
    }

    @Test
    void moneyValueToBigDecimal_whenNotNull() {
        final double value = 100;
        final MoneyValue moneyValue = TestData.createTinkoffMoneyValue(value, Currency.RUB);

        final BigDecimal bigDecimal = mapper.moneyValueToBigDecimal(moneyValue);

        AssertUtils.assertEquals(value, bigDecimal);
    }

    // endregion

    // region bigDecimalToMoneyValue tests

    @Test
    void bigDecimalToMoneyValue_whenNull() {
        final MoneyValue moneyValue = mapper.bigDecimalToMoneyValue(null);

        Assertions.assertNull(moneyValue);
    }

    @Test
    void bigDecimalToMoneyValue_whenNotNull() {
        final BigDecimal bigDecimal = DecimalUtils.setDefaultScale(100.000001);

        final MoneyValue moneyValue = mapper.bigDecimalToMoneyValue(bigDecimal);

        Assertions.assertEquals(100, moneyValue.getUnits());
        Assertions.assertEquals(1000, moneyValue.getNano());
        Assertions.assertEquals(StringUtils.EMPTY, moneyValue.getCurrency());
    }

    // endregion

    // region doubleToMoneyValue tests

    @Test
    void doubleToMoneyValue_whenBigDecimalIsNull() {
        final MoneyValue moneyValue = mapper.doubleToMoneyValue(null);

        Assertions.assertNull(moneyValue);
    }

    @Test
    void doubleToMoneyValue_whenNotNull() {
        final double doubleValue = 100.000001;

        final MoneyValue moneyValue = mapper.doubleToMoneyValue(doubleValue);

        Assertions.assertEquals(100, moneyValue.getUnits());
        Assertions.assertEquals(1000, moneyValue.getNano());
        Assertions.assertEquals(StringUtils.EMPTY, moneyValue.getCurrency());
    }

    // endregion

}