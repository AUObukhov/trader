package ru.obukhov.trader.test.utils.model.dividend;

import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.test.utils.ResourceUtils;

import java.util.Arrays;
import java.util.List;

public class TestDividends {

    public static final TestDividend TEST_DIVIDEND1 = readDividend("dividend1");
    public static final TestDividend TEST_DIVIDEND2 = readDividend("dividend2");
    public static final List<TestDividend> AAPL = readDividends("aapl");
    public static final List<TestDividend> MSFT = readDividends("msft");
    public static final List<TestDividend> BSPB = readDividends("bspb");
    public static final List<TestDividend> PIKK = readDividends("pikk");
    public static final List<TestDividend> GAZP = readDividends("gazp");
    public static final List<TestDividend> WOOSH = readDividends("wush");
    public static final List<TestDividend> TRANS_CONTAINER = readDividends("trcn");
    public static final List<TestDividend> SELIGDAR = readDividends("selg");

    private static TestDividend readDividend(final String ticker) {
        final Dividend dividend = ResourceUtils.getResourceAsObject("dividends/" + ticker + ".json", Dividend.class);
        return new TestDividend(dividend, Currencies.RUB);
    }

    private static List<TestDividend> readDividends(final String fileName) {
        final Dividend[] dividends = ResourceUtils.getResourceAsObject("dividends/" + fileName + ".json", Dividend[].class);
        return Arrays.stream(dividends).map(dividend -> new TestDividend(dividend, Currencies.RUB)).toList();
    }

}