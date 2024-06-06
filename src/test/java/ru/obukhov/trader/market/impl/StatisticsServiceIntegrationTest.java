package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.SetCapitalization;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

@SpringBootTest
class StatisticsServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private StatisticsService statisticsService;

    // region getExtendedCandles tests

    @Test
    void getExtendedCandles_simpleMovingAverage() {
        // arrange

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument);

        final String figi = instrument.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final List<Integer> prices = List.of(
                9912, 9898, 9876, 9897, 9897, 9898, 9885, 9896, 9888, 9888,
                9881, 9878, 9887, 9878, 9878, 9883, 9878, 9861, 9862, 9862
        );
        final OffsetDateTime startDateTime = DateTimeTestData.newDateTime(2020, 1, 1, 10);

        final List<HistoricCandle> historicCandles = TestData.newHistoricCandles(prices, startDateTime);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 2;
        final int bigWindow = 4;

        // act

        final GetCandlesResponse response = statisticsService.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

        // assert

        final List<Candle> expectedCandles = TestData.newCandles(prices, startDateTime);
        final List<BigDecimal> expectedShortAverages = TestData.newBigDecimalList(
                9912.0, 9905.0, 9887.0, 9886.5, 9897.0, 9897.5, 9891.5, 9890.5, 9892.0, 9888.0,
                9884.5, 9879.5, 9882.5, 9882.5, 9878.0, 9880.5, 9880.5, 9869.5, 9861.5, 9862.0
        );
        final List<BigDecimal> expectedLongAverages = TestData.newBigDecimalList(
                9912.0, 9905.0, 9895.333333333, 9895.75, 9892.0, 9892.0, 9894.25, 9894.0, 9891.75, 9889.25,
                9888.25, 9883.75, 9883.5, 9881.0, 9880.25, 9881.5, 9879.25, 9875.0, 9871.0, 9865.75
        );

        AssertUtils.assertEquals(expectedCandles, response.getCandles());
        AssertUtils.assertEquals(expectedShortAverages, response.getAverages1());
        AssertUtils.assertEquals(expectedLongAverages, response.getAverages2());
    }

    @Test
    void getExtendedCandles_simpleMovingAverage_whenToIsNull() {
        // arrange

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument);

        final String figi = instrument.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 1, 1);
        final Interval interval = Interval.of(from, null);

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final List<Integer> prices = List.of(
                9912, 9898, 9876, 9897, 9897, 9898, 9885, 9896, 9888, 9888,
                9881, 9878, 9887, 9878, 9878, 9883, 9878, 9861, 9862, 9862
        );
        final OffsetDateTime startDateTime = DateTimeTestData.newDateTime(2020, 1, 1, 10);

        final List<HistoricCandle> historicCandles = TestData.newHistoricCandles(prices, startDateTime);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 2;
        final int bigWindow = 4;

        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2020, 2, 1);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            // act

            final GetCandlesResponse response = statisticsService.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

            // assert

            final List<Candle> expectedCandles = TestData.newCandles(prices, startDateTime);
            final List<BigDecimal> expectedShortAverages = TestData.newBigDecimalList(
                    9912.0, 9905.0, 9887.0, 9886.5, 9897.0, 9897.5, 9891.5, 9890.5, 9892.0, 9888.0,
                    9884.5, 9879.5, 9882.5, 9882.5, 9878.0, 9880.5, 9880.5, 9869.5, 9861.5, 9862.0
            );
            final List<BigDecimal> expectedLongAverages = TestData.newBigDecimalList(
                    9912.0, 9905.0, 9895.333333333, 9895.75, 9892.0, 9892.0, 9894.25, 9894.0, 9891.75, 9889.25,
                    9888.25, 9883.75, 9883.5, 9881.0, 9880.25, 9881.5, 9879.25, 9875.0, 9871.0, 9865.75
            );

            AssertUtils.assertEquals(expectedCandles, response.getCandles());
            AssertUtils.assertEquals(expectedShortAverages, response.getAverages1());
            AssertUtils.assertEquals(expectedLongAverages, response.getAverages2());
        }
    }

    @Test
    void getExtendedCandles_linearWeightedMovingAverage() {
        // arrange

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument);

        final String figi = instrument.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final List<Integer> prices = List.of(
                9912, 9898, 9876, 9897, 9897, 9898, 9885, 9896, 9888, 9888,
                9881, 9878, 9887, 9878, 9878, 9883, 9878, 9861, 9862, 9862
        );
        final OffsetDateTime startDateTime = DateTimeTestData.newDateTime(2020, 1, 1, 10);

        final List<HistoricCandle> historicCandles = TestData.newHistoricCandles(prices, startDateTime);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 2;
        final int bigWindow = 4;

        // act

        final GetCandlesResponse response = statisticsService.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

        // assert

        final List<Candle> expectedCandles = TestData.newCandles(prices, startDateTime);
        final List<BigDecimal> expectedShortAverages = TestData.newBigDecimalList(
                9912.0, 9902.666666667, 9883.333333333, 9890.0, 9897.0, 9897.666666667, 9889.333333333, 9892.333333333, 9890.666666667, 9888.0,
                9883.333333333, 9879.0, 9884.0, 9881.0, 9878.0, 9881.333333333, 9879.666666667, 9866.666666667, 9861.666666667, 9862.0
        );
        final List<BigDecimal> expectedLongAverages = TestData.newBigDecimalList(
                9912.0, 9902.666666667, 9889.333333333, 9892.4, 9892.9, 9895.3, 9892.5, 9893.2, 9890.8, 9889.3,
                9886.0, 9881.9, 9883.2, 9881.0, 9879.8, 9880.9, 9879.5, 9872.2, 9867.0, 9863.4
        );

        AssertUtils.assertEquals(expectedCandles, response.getCandles());
        AssertUtils.assertEquals(expectedShortAverages, response.getAverages1());
        AssertUtils.assertEquals(expectedLongAverages, response.getAverages2());
    }

    @Test
    void getExtendedCandles_exponentialWeightedMovingAverage() {
        // arrange

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument);

        final String figi = instrument.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2020, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 2, 1);
        final Interval interval = Interval.of(from, to);

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final List<Integer> prices = List.of(
                9912, 9898, 9876, 9897, 9897, 9898, 9885, 9896, 9888, 9888,
                9881, 9878, 9887, 9878, 9878, 9883, 9878, 9861, 9862, 9862
        );
        final OffsetDateTime startDateTime = DateTimeTestData.newDateTime(2020, 1, 1, 10);

        final List<HistoricCandle> historicCandles = TestData.newHistoricCandles(prices, startDateTime);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final MovingAverageType movingAverageType = MovingAverageType.EXPONENTIAL_WEIGHTED;
        final int smallWindow = 2;
        final int bigWindow = 4;

        // act

        final GetCandlesResponse response = statisticsService.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

        // assert

        final List<Candle> expectedCandles = TestData.newCandles(prices, startDateTime);
        final List<BigDecimal> expectedShortAverages = TestData.newBigDecimalList(
                9912.0, 9902.666666667, 9884.888888889, 9892.962962963, 9895.654320988,
                9897.218106996, 9889.072702332, 9893.690900777, 9889.896966926, 9888.632322309,
                9883.544107436, 9879.848035812, 9884.616011937, 9880.205337312, 9878.735112437,
                9881.578370813, 9879.192790271, 9867.064263424, 9863.688087808, 9862.562695936
        );
        final List<BigDecimal> expectedLongAverages = TestData.newBigDecimalList(
                9912.0, 9906.4, 9894.24, 9895.344, 9896.0064,
                9896.80384, 9892.082304, 9893.6493824, 9891.38962944, 9890.033777664,
                9886.420266598, 9883.052159959, 9884.631295975, 9881.978777585, 9880.387266551,
                9881.432359931, 9880.059415959, 9872.435649575, 9868.261389745, 9865.756833847
        );

        AssertUtils.assertEquals(expectedCandles, response.getCandles());
        AssertUtils.assertEquals(expectedShortAverages, response.getAverages1());
        AssertUtils.assertEquals(expectedLongAverages, response.getAverages2());
    }


    // endregion

    @Test
    void getIndexWeights() {
        // arrange

        final ru.tinkoff.piapi.contract.v1.Share share1 = TestShares.APPLE.tinkoffShare();
        final ru.tinkoff.piapi.contract.v1.Share share2 = TestShares.SBER.tinkoffShare();
        final ru.tinkoff.piapi.contract.v1.Share share3 = TestShares.YANDEX.tinkoffShare();

        final SequencedMap<Share, Double> sharesLastPrices = new LinkedHashMap<>(3, 1);
        sharesLastPrices.put(share1, 178.7);
        sharesLastPrices.put(share2, 258.79);
        sharesLastPrices.put(share3, 2585.6);
        Mocker.mockSharesLastPrices(instrumentsService, marketDataService, sharesLastPrices);

        final SequencedMap<ru.tinkoff.piapi.contract.v1.Currency, Double> currenciesLastPrices = new LinkedHashMap<>(2, 1);
        currenciesLastPrices.put(TestCurrencies.USD.tinkoffCurrency(), 98.4225);
        currenciesLastPrices.put(TestCurrencies.RUB.tinkoffCurrency(), 1.0);
        Mocker.mockCurrenciesLastPrices(instrumentsService, marketDataService, currenciesLastPrices);

        final List<String> shareFigies = sharesLastPrices.keySet().stream()
                .map(ru.tinkoff.piapi.contract.v1.Share::getFigi)
                .toList();

        // act

        final SequencedMap<String, BigDecimal> actualResult = statisticsService.getCapitalizationWeights(shareFigies);

        // assert

        final SequencedMap<String, BigDecimal> expectedWeights = new LinkedHashMap<>(3, 1);
        expectedWeights.put(share1.getFigi(), DecimalUtils.setDefaultScale(0.978361221));
        expectedWeights.put(share2.getFigi(), DecimalUtils.setDefaultScale(0.018799306));
        expectedWeights.put(share3.getFigi(), DecimalUtils.setDefaultScale(0.002839473));

        Assertions.assertEquals(expectedWeights, actualResult);
    }

    @Test
    void getCapitalization() {
        // arrange

        final ru.tinkoff.piapi.contract.v1.Share share1 = TestShares.APPLE.tinkoffShare();
        final ru.tinkoff.piapi.contract.v1.Share share2 = TestShares.SBER.tinkoffShare();
        final ru.tinkoff.piapi.contract.v1.Share share3 = TestShares.YANDEX.tinkoffShare();

        final SequencedMap<ru.tinkoff.piapi.contract.v1.Share, Double> sharesLastPrices = new LinkedHashMap<>(3, 1);
        sharesLastPrices.put(share1, 178.7);
        sharesLastPrices.put(share2, 258.79);
        sharesLastPrices.put(share3, 2585.6);
        Mocker.mockSharesLastPrices(instrumentsService, marketDataService, sharesLastPrices);

        final SequencedMap<ru.tinkoff.piapi.contract.v1.Currency, Double> currenciesLastPrices = new LinkedHashMap<>(2, 1);
        currenciesLastPrices.put(TestCurrencies.USD.tinkoffCurrency(), 98.4225);
        currenciesLastPrices.put(TestCurrencies.RUB.tinkoffCurrency(), 1.0);
        Mocker.mockCurrenciesLastPrices(instrumentsService, marketDataService, currenciesLastPrices);

        final List<String> shareFigies = sharesLastPrices.keySet().stream()
                .map(ru.tinkoff.piapi.contract.v1.Share::getFigi)
                .toList();

        // act

        final SetCapitalization actualResult = statisticsService.getCapitalization(shareFigies);

        // assert

        final Map<String, BigDecimal> securitiesCapitalizations = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(290734225022224.5),
                share2.getFigi(), DecimalUtils.setDefaultScale(5586486272920L),
                share3.getFigi(), DecimalUtils.setDefaultScale(843790573312L)
        );
        final BigDecimal totalCapitalization = DecimalUtils.setDefaultScale(297164501868456.5);
        final SetCapitalization expectedResult = new SetCapitalization(securitiesCapitalizations, totalCapitalization);

        Assertions.assertEquals(expectedResult, actualResult);
    }

}