package ru.obukhov.trader.test.utils.model.dividend;

import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestDividends {

    public static final TestDividend TEST_DIVIDEND1 = readDividend("dividend1.json");
    public static final TestDividend TEST_DIVIDEND2 = readDividend("dividend2.json");

    private static TestDividend readDividend(final String fileName) {
        final Dividend dividend = ResourceUtils.getResourceAsObject("dividends/" + fileName, Dividend.class);
        return new TestDividend(dividend, Currencies.RUB);
    }

}