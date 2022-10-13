package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;

class CurrencyInstrumentMapperUnitTest {

    private final CurrencyInstrumentMapper currencyInstrumentMapper = Mappers.getMapper(CurrencyInstrumentMapper.class);

    @Test
    void map() {
        final CurrencyInstrument result = currencyInstrumentMapper.map(TestCurrency1.TINKOFF_CURRENCY);

        Assertions.assertEquals(TestCurrency1.CURRENCY, result);
    }

    @Test
    void map_whenValueIsNull() {
        final CurrencyInstrument currencyInstrument = currencyInstrumentMapper.map(null);

        Assertions.assertNull(currencyInstrument);
    }

}