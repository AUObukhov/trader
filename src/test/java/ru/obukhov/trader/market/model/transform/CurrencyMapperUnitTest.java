package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;

class CurrencyMapperUnitTest {

    private final CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);

    @Test
    void map() {
        final TestCurrency testCurrency = TestCurrencies.USD;

        final Currency result = currencyMapper.map(testCurrency.tinkoffCurrency());

        Assertions.assertEquals(testCurrency.currency(), result);
    }

    @Test
    void map_whenValueIsNull() {
        final Currency currency = currencyMapper.map(null);

        Assertions.assertNull(currency);
    }

}