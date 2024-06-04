package ru.obukhov.trader.test.utils.model.currency;

import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestCurrencies {

    public static final TestCurrency USD = readCurrency("usd.json");
    public static final TestCurrency RUB = readCurrency("rub.json");
    public static final TestCurrency CNY = readCurrency("cny.json");

    private static TestCurrency readCurrency(final String fileName) {
        final Currency currency = ResourceUtils.getResourceAsObject("currencies/" + fileName, Currency.class);
        return new TestCurrency(currency);
    }

}