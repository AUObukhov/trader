package ru.obukhov.trader.test.utils.model.currency;

import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestCurrencies {

    public static final TestCurrency USD = readCurrencyWithCandles("usd");
    public static final TestCurrency RUB = readCurrency("rub");
    public static final TestCurrency CNY = readCurrency("cny");

    private static TestCurrency readCurrency(final String ticker) {
        final Currency currency = ResourceUtils.getResourceAsObject("currencies/" + ticker + ".json", Currency.class);
        return new TestCurrency(currency, Collections.emptyMap());
    }

    private static TestCurrency readCurrencyWithCandles(final String ticker) {
        final Currency currency = ResourceUtils.getResourceAsObject("currencies/" + ticker + ".json", Currency.class);
        final List<HistoricCandle> minCandles = TestUtils.getHistoricCandles(ticker + "-1min.csv");
        final List<HistoricCandle> dayCandles = TestUtils.getHistoricCandles(ticker + "-day.csv");
        final List<HistoricCandle> monthCandles = TestUtils.getHistoricCandles(ticker + "-month.csv");
        final Map<CandleInterval, List<HistoricCandle>> candles = Map.of(
                CandleInterval.CANDLE_INTERVAL_1_MIN, minCandles,
                CandleInterval.CANDLE_INTERVAL_DAY, dayCandles,
                CandleInterval.CANDLE_INTERVAL_MONTH, monthCandles
        );
        return new TestCurrency(currency, candles);
    }

}