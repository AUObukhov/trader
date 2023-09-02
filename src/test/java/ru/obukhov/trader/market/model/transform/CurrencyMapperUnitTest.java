package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;

class CurrencyMapperUnitTest {

    private final CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);

    @Test
    void map() {
        final Currency result = currencyMapper.map(TestCurrency1.TINKOFF_CURRENCY);

        Assertions.assertEquals(TestCurrency1.CURRENCY, result);
    }

    @Test
    void map_whenValueIsNull() {
        final Currency currency = currencyMapper.map(null);

        Assertions.assertNull(currency);
    }

}