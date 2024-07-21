package ru.obukhov.trader.test.utils.model.share;

import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.dividend.TestDividend;
import ru.obukhov.trader.test.utils.model.dividend.TestDividends;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestShares {

    public static final TestShare APPLE = buildShare("aapl", TestDividends.AAPL);
    public static final TestShare AVAILABLE_APPLE = TestShares.APPLE
            .withForQualInvestorFlag(false)
            .withForIisFlag(true);
    public static final TestShare MICROSOFT = buildShare("msft", TestDividends.MSFT);
    public static final TestShare SBER = buildShare("sber");
    public static final TestShare YANDEX = buildShare("yndx");
    public static final TestShare DIOD = buildShare("diod");
    public static final TestShare SPB_BANK = buildShare("bspb", TestDividends.BSPB);
    public static final TestShare PIK = buildShare("pikk", TestDividends.PIKK);
    public static final TestShare GAZPROM = buildShare("gazp", TestDividends.GAZP);
    public static final TestShare RBC = buildShare("rbcm", Collections.emptyList());
    public static final TestShare WOOSH = buildShare("wush", TestDividends.WOOSH);
    public static final TestShare TRANS_CONTAINER = buildShare("trcn", TestDividends.TRANS_CONTAINER);
    public static final TestShare SELIGDAR = buildShare("selg", TestDividends.SELIGDAR);

    private static TestShare buildShare(final String ticker) {
        return buildShare(ticker, Collections.emptyList(), Collections.emptyMap());
    }

    private static TestShare buildShare(final String ticker, final List<TestDividend> dividends) {
        final Share share = ResourceUtils.getResourceAsObject("shares/" + ticker + ".json", Share.class);
        final List<HistoricCandle> minCandles = TestUtils.getHistoricCandles(ticker + "-1min.csv");
        final List<HistoricCandle> monthCandles = TestUtils.getHistoricCandles(ticker + "-month.csv");
        final Map<CandleInterval, List<HistoricCandle>> candles = Map.of(
                CandleInterval.CANDLE_INTERVAL_1_MIN, minCandles,
                CandleInterval.CANDLE_INTERVAL_MONTH, monthCandles
        );
        return new TestShare(share, dividends, candles);
    }

    private static TestShare buildShare(
            final String ticker,
            final List<TestDividend> dividends,
            final Map<CandleInterval, List<HistoricCandle>> candles
    ) {
        final Share share = ResourceUtils.getResourceAsObject("shares/" + ticker + ".json", Share.class);
        return new TestShare(share, dividends, candles);
    }

}