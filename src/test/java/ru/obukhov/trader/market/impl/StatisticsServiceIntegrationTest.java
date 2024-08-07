package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.InstrumentMarker;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.SetCapitalization;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.SharesFiltrationOptions;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.stream.Stream;

@SpringBootTest
class StatisticsServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private StatisticsService statisticsService;

    // region getExtendedCandles tests

    @Test
    void getExtendedCandles_simpleMovingAverage() {
        // arrange

        final TestInstrument instrument = TestInstruments.APPLE;

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

        final TestInstrument instrument = TestInstruments.APPLE;

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

        final TestInstrument instrument = TestInstruments.APPLE;

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

        final TestInstrument instrument = TestInstruments.APPLE;

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
    @DirtiesContext
    void getIndexWeights() {
        // arrange

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;
        final TestShare share3 = TestShares.YANDEX;

        final SequencedMap<TestShare, Double> sharesLastPrices = new LinkedHashMap<>(3, 1);
        sharesLastPrices.put(share1, 178.7);
        sharesLastPrices.put(share2, 258.79);
        sharesLastPrices.put(share3, 2585.6);
        Mocker.mockSharesLastPrices(instrumentsService, marketDataService, sharesLastPrices);

        final SequencedMap<TestCurrency, Double> currenciesLastPrices = new LinkedHashMap<>(2, 1);
        currenciesLastPrices.put(TestCurrencies.USD, 98.4225);
        currenciesLastPrices.put(TestCurrencies.RUB, 1.0);
        Mocker.mockCurrenciesLastPrices(instrumentsService, marketDataService, currenciesLastPrices);

        final List<String> shareFigies = sharesLastPrices.keySet().stream()
                .map(TestShare::getFigi)
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
    @DirtiesContext
    void getCapitalization() {
        // arrange

        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;
        final TestShare share3 = TestShares.YANDEX;

        final SequencedMap<TestShare, Double> sharesLastPrices = new LinkedHashMap<>(3, 1);
        sharesLastPrices.put(share1, 178.7);
        sharesLastPrices.put(share2, 258.79);
        sharesLastPrices.put(share3, 2585.6);
        Mocker.mockSharesLastPrices(instrumentsService, marketDataService, sharesLastPrices);

        final SequencedMap<TestCurrency, Double> currenciesLastPrices = new LinkedHashMap<>(2, 1);
        currenciesLastPrices.put(TestCurrencies.USD, 98.4225);
        currenciesLastPrices.put(TestCurrencies.RUB, 1.0);
        Mocker.mockCurrenciesLastPrices(instrumentsService, marketDataService, currenciesLastPrices);

        final List<String> shareFigies = sharesLastPrices.keySet().stream()
                .map(TestShare::getFigi)
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

    // region getMostProfitableShares tests

    private static final SharesFiltrationOptions BASIC_FILTRATION_OPTIONS = new SharesFiltrationOptions(
            List.of(Currencies.RUB),
            true,
            false,
            true,
            List.of(ShareType.SHARE_TYPE_COMMON, ShareType.SHARE_TYPE_PREFERRED),
            3652,
            700,
            true
    );

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetMostProfitableShares() {
        return Stream.of(
                getArgumentsForGetMostProfitableShares_noFiltration(),
                getArgumentsForGetMostProfitableShares_currenciesNull(),
                getArgumentsForGetMostProfitableShares_currenciesEmpty(),
                getArgumentsForGetMostProfitableShares_currenciesUsd(),
                getArgumentsForGetMostProfitableShares_apiTradeAvailableFlagNull(),
                getArgumentsForGetMostProfitableShares_apiTradeAvailableFlagFalse(),
                getArgumentsForGetMostProfitableShares_forQualInvestorFlagNull(),
                getArgumentsForGetMostProfitableShares_forQualInvestorFlagTrue(),
                getArgumentsForGetMostProfitableShares_forIisFlagNull(),
                getArgumentsForGetMostProfitableShares_forIisFlagFalse(),
                getArgumentsForGetMostProfitableShares_shareTypesNull(),
                getArgumentsForGetMostProfitableShares_shareTypesEmpty(),
                getArgumentsForGetMostProfitableShares_shareTypesAdr(),
                getArgumentsForGetMostProfitableShares_minTradingDaysNull(),
                getArgumentsForGetMostProfitableShares_minTradingDays365(),
                getArgumentsForGetMostProfitableShares_havingDividendsWithinDaysNull(),
                getArgumentsForGetMostProfitableShares_havingDividendsWithinDays2000(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByRegularInvestingAnnualReturns(),
                getArgumentsForGetMostProfitableShares_fullFiltration()
        );
    }

    private static Arguments getArgumentsForGetMostProfitableShares_noFiltration() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.MICROSOFT,
                TestShares.WOOSH,
                TestShares.GAZPROM,
                TestShares.APPLE,
                TestShares.TRANS_CONTAINER,
                TestShares.SELIGDAR,
                TestShares.RBC
        );

        final SharesFiltrationOptions filtrationOptions = new SharesFiltrationOptions(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestShares.GAZPROM.share(), 0.012762569152802827);
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.PIK.share(), 0.057035507311362865);
        expectedResult.put(TestShares.APPLE.share(), 0.05739874688830482);
        expectedResult.put(TestShares.MICROSOFT.share(), 0.058288438156771205);
        expectedResult.put(TestShares.RBC.share(), 0.06186532835804104);
        expectedResult.put(TestShares.SELIGDAR.share(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);
        expectedResult.put(TestShares.TRANS_CONTAINER.share(), 0.10703759628725629);
        expectedResult.put(TestShares.WOOSH.share(), 0.1424311683598216);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_currenciesNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.AVAILABLE_APPLE
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withCurrencies(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.AVAILABLE_APPLE.share(), 0.05739874688830482);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_currenciesEmpty() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.AVAILABLE_APPLE
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withCurrencies(Collections.emptyList());

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.AVAILABLE_APPLE.share(), 0.05739874688830482);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_currenciesUsd() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.AVAILABLE_APPLE
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withCurrencies(List.of(Currencies.USD));

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.AVAILABLE_APPLE.share(), 0.05739874688830482);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_apiTradeAvailableFlagNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withApiTradeAvailableFlag(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);
        expectedResult.put(TestShares.TRANS_CONTAINER.share(), 0.10703759628725629);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_apiTradeAvailableFlagFalse() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withApiTradeAvailableFlag(false);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.TRANS_CONTAINER.share(), 0.10703759628725629);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_forQualInvestorFlagNull() {
        final TestShare availableSeligdar = TestShares.SELIGDAR.withForQualInvestorFlag(true);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                availableSeligdar
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withForQualInvestorFlag(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(availableSeligdar.share(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_forQualInvestorFlagTrue() {
        final TestShare availableSeligdar = TestShares.SELIGDAR.withForQualInvestorFlag(true);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                availableSeligdar
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withForQualInvestorFlag(true);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(availableSeligdar.share(), 0.10300118574872186);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_forIisFlagNull() {
        final TestShare availableSeligdar = TestShares.SELIGDAR.withForIisFlag(false);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                availableSeligdar
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withForIisFlag(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(availableSeligdar.share(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_forIisFlagFalse() {
        final TestShare availableSeligdar = TestShares.SELIGDAR.withForIisFlag(false);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                availableSeligdar
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withForIisFlag(false);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(availableSeligdar.share(), 0.10300118574872186);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_shareTypesNull() {
        final TestShare availableSeligdar = TestShares.SELIGDAR.withShareType(ShareType.SHARE_TYPE_ADR);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                availableSeligdar
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withShareTypes(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(availableSeligdar.share(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_shareTypesEmpty() {
        final TestShare availableSeligdar = TestShares.SELIGDAR.withShareType(ShareType.SHARE_TYPE_ADR);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                availableSeligdar
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withShareTypes(Collections.emptyList());

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(availableSeligdar.share(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_shareTypesAdr() {
        final TestShare seligdarAdr = TestShares.SELIGDAR.withShareType(ShareType.SHARE_TYPE_ADR);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarAdr
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withShareTypes(List.of(ShareType.SHARE_TYPE_ADR));

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(seligdarAdr.share(), 0.10300118574872186);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_minTradingDaysNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withMinTradingDays(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);
        expectedResult.put(TestShares.WOOSH.share(), 0.1424311683598216);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_minTradingDays365() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withMinTradingDays(365);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);
        expectedResult.put(TestShares.WOOSH.share(), 0.1424311683598216);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_havingDividendsWithinDaysNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.GAZPROM,
                TestShares.PIK,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withHavingDividendsWithinDays(null);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.PIK.share(), 0.057035507311362865);
        expectedResult.put(TestShares.RBC.share(), 0.06186532835804104);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_havingDividendsWithinDays2000() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.GAZPROM,
                TestShares.PIK,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS.withHavingDividendsWithinDays(2000);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.PIK.share(), 0.057035507311362865);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByRegularInvestingAnnualReturns() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = BASIC_FILTRATION_OPTIONS
                .withFilterByRegularInvestingAnnualReturns(false);

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestShares.GAZPROM.share(), 0.012762569152802827);
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_fullFiltration() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SequencedMap<Object, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.currency(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.share(), 0.1030930657724991);

        return Arguments.of(shares, BASIC_FILTRATION_OPTIONS, expectedResult);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetMostProfitableShares")
    @DirtiesContext
    void getMostProfitableShares(
            final List<TestShare> shares,
            final SharesFiltrationOptions filtrationOptions,
            final SequencedMap<Object, Double> expectedResult
    ) {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2024, 6, 1);
        Mocker.mockCurrency(instrumentsService, marketDataService, TestCurrencies.USD);
        Mocker.mockAllCurrencies(instrumentsService, TestCurrencies.USD, TestCurrencies.RUB);
        Mocker.mockAllShares(instrumentsService, marketDataService, shares, mockedNow);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final SequencedMap<InstrumentMarker, Double> actualResult = statisticsService.getMostProfitableShares(filtrationOptions);
            AssertUtils.assertEquals(expectedResult, actualResult);
        }
    }

    // endregion

}