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
import ru.obukhov.trader.market.model.*;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.SharesFiltrationOptions;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.exchange.WeightedShare;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.ShareType;
import ru.tinkoff.piapi.core.models.Portfolio;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withCurrencies(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withCurrencies(Collections.emptyList());

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withCurrencies(List.of(Currencies.USD));

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withApiTradeAvailableFlag(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withApiTradeAvailableFlag(false);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForQualInvestorFlag(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForQualInvestorFlag(true);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForIisFlag(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForIisFlag(false);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withShareTypes(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withShareTypes(Collections.emptyList());

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withShareTypes(List.of(ShareType.SHARE_TYPE_ADR));

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withMinTradingDays(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withMinTradingDays(365);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withHavingDividendsWithinDays(null);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withHavingDividendsWithinDays(2000);

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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS
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

        return Arguments.of(shares, TestData.BASIC_SHARE_FILTRATION_OPTIONS, expectedResult);
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

    // region getWeightedShares tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetWeightedShares() {
        return Stream.of(
                getArgumentsForGetWeightedShares_noFiltration(),
                getArgumentsForGetWeightedShares_currenciesNull(),
                getArgumentsForGetWeightedShares_currenciesEmpty(),
                getArgumentsForGetWeightedShares_currenciesUsd(),
                getArgumentsForGetWeightedShares_apiTradeAvailableFlagNull(),
                getArgumentsForGetWeightedShares_apiTradeAvailableFlagFalse(),
                getArgumentsForGetWeightedShares_forQualInvestorFlagNull(),
                getArgumentsForGetWeightedShares_forQualInvestorFlagTrue(),
                getArgumentsForGetWeightedShares_forIisFlagNull(),
                getArgumentsForGetWeightedShares_forIisFlagFalse(),
                getArgumentsForGetWeightedShares_shareTypesNull(),
                getArgumentsForGetWeightedShares_shareTypesEmpty(),
                getArgumentsForGetWeightedShares_shareTypesAdr(),
                getArgumentsForGetWeightedShares_minTradingDaysNull(),
                getArgumentsForGetWeightedShares_minTradingDays365(),
                getArgumentsForGetWeightedShares_havingDividendsWithinDaysNull(),
                getArgumentsForGetWeightedShares_havingDividendsWithinDays2000(),
                getArgumentsForGetWeightedShares_excludeFiltrationByRegularInvestingAnnualReturns(),
                getArgumentsForGetWeightedShares_fullFiltration()
        );
    }

    private static Arguments getArgumentsForGetWeightedShares_noFiltration() {
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

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.000265210,
                0.002551175,
                -0.896043980
        );
        final WeightedShare weightedSharePikk = TestData.newWeightedShare(
                TestShares.PIK,
                accountsToPortfolios,
                1,
                0.000940233,
                0.000313269,
                2.001359854
        );
        final WeightedShare weightedShareMsft = TestData.newWeightedShare(
                TestShares.MICROSOFT,
                accountsToPortfolios,
                90.1,
                0.503282430,
                0.147381406,
                2.414829887
        );
        final WeightedShare weightedShareWush = TestData.newWeightedShare(
                TestShares.WOOSH,
                accountsToPortfolios,
                1,
                0.000051649,
                0.004694163,
                -0.988997187
        );
        final WeightedShare weightedShareGazp = TestData.newWeightedShare(
                TestShares.GAZPROM,
                accountsToPortfolios,
                1,
                0.005085235,
                0.001418153,
                2.585815494
        );
        final WeightedShare weightedShareAapl = TestData.newWeightedShare(
                TestShares.APPLE,
                accountsToPortfolios,
                90.1,
                0.490046287,
                0.652399343,
                -0.248855333
        );
        final WeightedShare weightedShareTrcn = TestData.newWeightedShare(
                TestShares.TRANS_CONTAINER,
                accountsToPortfolios,
                1,
                0.000203599,
                0.003224611,
                -0.936860911
        );
        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                TestShares.SELIGDAR,
                accountsToPortfolios,
                1,
                0.000114138,
                0.029995070,
                -0.996194775
        );
        final WeightedShare weightedShareRbcm = TestData.newWeightedShare(
                TestShares.RBC,
                accountsToPortfolios,
                1,
                0.00001122,
                0.158022809,
                -0.999928998
        );

        final List<WeightedShare> expectedResult = List.of(
                weightedShareGazp,
                weightedShareMsft,
                weightedShareAapl,
                weightedSharePikk,
                weightedShareRbcm,
                weightedShareSelg,
                weightedShareBspb,
                weightedShareTrcn,
                weightedShareWush
        );

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_currenciesNull() {
        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.AVAILABLE_APPLE
        );

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withCurrencies(null);

        final WeightedShare weightedShareSpb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.0005409007,
                0.0038952186,
                -0.861137204
        );
        final WeightedShare weightedShareApple = TestData.newWeightedShare(
                TestShares.AVAILABLE_APPLE,
                accountsToPortfolios,
                90.1,
                0.99945909933,
                0.99610478138,
                0.00336743484
        );
        final List<WeightedShare> expectedResult = List.of(weightedShareApple, weightedShareSpb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_currenciesEmpty() {
        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.AVAILABLE_APPLE
        );

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withCurrencies(Collections.emptyList());

        final WeightedShare weightedShareSpb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.0005409007,
                0.0038952186,
                -0.861137204
        );
        final WeightedShare weightedShareApple = TestData.newWeightedShare(
                TestShares.AVAILABLE_APPLE,
                accountsToPortfolios,
                90.1,
                0.99945909933,
                0.99610478138,
                0.00336743484
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareApple, weightedShareSpb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_currenciesUsd() {
        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.AVAILABLE_MICROSOFT,
                TestShares.AVAILABLE_APPLE
        );

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withCurrencies(List.of(Currencies.USD));

        final WeightedShare weightedShareMicrosoft = TestData.newWeightedShare(
                TestShares.AVAILABLE_MICROSOFT,
                accountsToPortfolios,
                90.1,
                0.506662519,
                0.184277261,
                1.749457618
        );
        final WeightedShare weightedShareApple = TestData.newWeightedShare(
                TestShares.AVAILABLE_APPLE,
                accountsToPortfolios,
                90.1,
                0.493337481,
                0.815722739,
                -0.395214259
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareMicrosoft, weightedShareApple);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_apiTradeAvailableFlagNull() {
        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withApiTradeAvailableFlag(null);

        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.565710165,
                0.441701851,
                0.280751176
        );
        final WeightedShare weightedShareTrcn = TestData.newWeightedShare(
                TestShares.TRANS_CONTAINER,
                accountsToPortfolios,
                1,
                0.434289835,
                0.558298149,
                -0.222118440
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareBspb, weightedShareTrcn);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_apiTradeAvailableFlagFalse() {
        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withApiTradeAvailableFlag(false);

        final WeightedShare weightedShareTrcn = TestData.newWeightedShare(
                TestShares.TRANS_CONTAINER,
                accountsToPortfolios,
                1,
                1,
                1,
                0
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareTrcn);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_forQualInvestorFlagNull() {
        final TestShare seligdarForQual = TestShares.SELIGDAR.withForQualInvestorFlag(true);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarForQual
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForQualInvestorFlag(null);

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarForQual,
                accountsToPortfolios,
                1,
                0.300878632,
                0.921613833,
                -0.673530690
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.699121368,
                0.078386167,
                7.918938057
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_forQualInvestorFlagTrue() {
        final TestShare seligdarForQual = TestShares.SELIGDAR.withForQualInvestorFlag(true);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarForQual
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForQualInvestorFlag(true);

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarForQual,
                accountsToPortfolios,
                1,
                1,
                1,
                0
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_forIisFlagNull() {
        final TestShare seligdarNotIis = TestShares.SELIGDAR.withForIisFlag(false);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarNotIis
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForIisFlag(null);

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarNotIis,
                accountsToPortfolios,
                1,
                0.300878632,
                0.921613833,
                -0.673530690
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.699121368,
                0.078386167,
                7.918938057
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_forIisFlagFalse() {
        final TestShare seligdarNotIis = TestShares.SELIGDAR.withForIisFlag(false);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarNotIis
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withForIisFlag(false);

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarNotIis,
                accountsToPortfolios,
                1,
                1,
                1,
                0
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_shareTypesNull() {
        final TestShare seligdarTypeAdr = TestShares.SELIGDAR.withShareType(ShareType.SHARE_TYPE_ADR);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarTypeAdr
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withShareTypes(null);

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarTypeAdr,
                accountsToPortfolios,
                1,
                0.300878632,
                0.921613833,
                -0.673530690
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.699121368,
                0.078386167,
                7.918938057
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_shareTypesEmpty() {
        final TestShare seligdarTypeAdr = TestShares.SELIGDAR.withShareType(ShareType.SHARE_TYPE_ADR);
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                seligdarTypeAdr
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withShareTypes(Collections.emptyList());

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarTypeAdr,
                accountsToPortfolios,
                1,
                0.300878632,
                0.921613833,
                -0.673530690
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.699121368,
                0.078386167,
                7.918938057
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_shareTypesAdr() {
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

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withShareTypes(List.of(ShareType.SHARE_TYPE_ADR));

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final WeightedShare weightedShareSelg = TestData.newWeightedShare(
                seligdarAdr,
                accountsToPortfolios,
                1,
                1,
                1,
                0
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareSelg);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_minTradingDaysNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withMinTradingDays(null);

        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.836996342,
                0.352112676,
                1.377069612
        );
        final WeightedShare weightedShareWush = TestData.newWeightedShare(
                TestShares.WOOSH,
                accountsToPortfolios,
                1,
                0.163003658,
                0.647887324,
                -0.748407397
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareBspb, weightedShareWush);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_minTradingDays365() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withMinTradingDays(365);

        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.836996342,
                0.352112676,
                1.377069612
        );
        final WeightedShare weightedShareWush = TestData.newWeightedShare(
                TestShares.WOOSH,
                accountsToPortfolios,
                1,
                0.163003658,
                0.647887324,
                -0.748407397
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareBspb, weightedShareWush);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_havingDividendsWithinDaysNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.GAZPROM,
                TestShares.PIK,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withHavingDividendsWithinDays(null);

        final WeightedShare weightedSharePikk = TestData.newWeightedShare(
                TestShares.PIK,
                accountsToPortfolios,
                1,
                0.772796622,
                0.001947136,
                395.888877818
        );
        final WeightedShare weightedShareRbcm = TestData.newWeightedShare(
                TestShares.RBC,
                accountsToPortfolios,
                1,
                0.009221959,
                0.982195949,
                -0.990610877
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.217981419,
                0.015856915,
                12.746773505
        );

        final List<WeightedShare> expectedResult = List.of(weightedSharePikk, weightedShareRbcm, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_havingDividendsWithinDays2000() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.GAZPROM,
                TestShares.PIK,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withHavingDividendsWithinDays(2000);

        final WeightedShare weightedSharePikk = TestData.newWeightedShare(
                TestShares.PIK,
                accountsToPortfolios,
                1,
                0.779989655,
                0.109364768,
                6.132001185
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.220010345,
                0.890635232,
                -0.752973679
        );

        final List<WeightedShare> expectedResult = List.of(weightedSharePikk, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_excludeFiltrationByRegularInvestingAnnualReturns() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS.withFilterByRegularInvestingAnnualReturns(false);

        final WeightedShare weightedSharePikk = TestData.newWeightedShare(
                TestShares.GAZPROM,
                accountsToPortfolios,
                1,
                0.950432194,
                0.357277883,
                1.660204393
        );
        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                0.049567806,
                0.642722117,
                -0.922878325
        );

        final List<WeightedShare> expectedResult = List.of(weightedSharePikk, weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_fullFiltration() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                1,
                1,
                0
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, TestData.BASIC_SHARE_FILTRATION_OPTIONS, expectedResult);
    }

    private static Map<String, Portfolio> getAccountsToPortfoliosArgument() {
        final Portfolio portfolio1 = TestData.newPortfolio(
                TestData.newPortfolioPosition(TestShares.SPB_BANK, 2, 340),
                TestData.newPortfolioPosition(TestShares.PIK, 1, 835),
                TestData.newPortfolioPosition(TestShares.MICROSOFT, 10, 436),
                TestData.newPortfolioPosition(TestShares.WOOSH, 46, 272),
                TestData.newPortfolioPosition(TestShares.GAZPROM, 3, 126)
        );
        final Portfolio portfolio2 = TestData.newPortfolio(
                TestData.newPortfolioPosition(TestShares.AVAILABLE_APPLE, 100, 193),
                TestData.newPortfolioPosition(TestShares.TRANS_CONTAINER, 1, 8595),
                TestData.newPortfolioPosition(TestShares.SELIGDAR, 123, 65),
                TestData.newPortfolioPosition(TestShares.RBC, 234, 18)
        );
        return Map.of(
                TestAccounts.IIS.getId(), portfolio1,
                TestAccounts.TINKOFF.getId(), portfolio2
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetWeightedShares")
    @DirtiesContext
    void getWeightedShares(
            final Map<String, Portfolio> accountsToPortfolios,
            final List<TestShare> shares,
            final SharesFiltrationOptions filtrationOptions,
            final List<WeightedShare> expectedResult
    ) {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2024, 6, 1);
        Mocker.mockCurrency(instrumentsService, marketDataService, TestCurrencies.USD);
        Mocker.mockAllCurrencies(instrumentsService, TestCurrencies.USD, TestCurrencies.RUB);
        Mocker.mockAllShares(instrumentsService, marketDataService, shares, mockedNow);
        Mocker.mockPortfolios(operationsService, accountsToPortfolios);
        final List<String> figies = expectedResult.stream().map(WeightedShare::getFigi).toList();
        Mocker.mockLastPrices(marketDataService, accountsToPortfolios.values(), figies);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final List<WeightedShare> actualResult = statisticsService.getWeightedShares(accountsToPortfolios.keySet(), filtrationOptions);
            AssertUtils.assertEquals(expectedResult, actualResult);
        }
    }

    // endregion

}