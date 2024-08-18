package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.test.utils.model.dividend.TestDividend;
import ru.obukhov.trader.test.utils.model.dividend.TestDividends;

class DividendMapperUnitTest {

    private static final DividendMapper DIVIDEND_MAPPER = Mappers.getMapper(DividendMapper.class);

    @Test
    void map() {
        final TestDividend testDividend = TestDividends.TEST_DIVIDEND1;

        final Dividend actualResult = DIVIDEND_MAPPER.map(testDividend.tDividend());

        Assertions.assertEquals(testDividend.dividend(), actualResult);
    }

    @Test
    void map_whenValueIsNull() {
        final Dividend dividend = DIVIDEND_MAPPER.map(null);

        Assertions.assertNull(dividend);
    }

}