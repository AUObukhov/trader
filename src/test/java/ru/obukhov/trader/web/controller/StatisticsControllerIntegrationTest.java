package ru.obukhov.trader.web.controller;

import org.apache.commons.lang3.BooleanUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class StatisticsControllerIntegrationTest extends ControllerIntegrationTest {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);

    @MockBean
    private ExcelService excelService;

    // region getCandles tests

    @Test
    void getCandles_returnsBadRequest_whenFigiIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name())
                .param("movingAverageType", MovingAverageType.LINEAR_WEIGHTED.getValue())
                .param("smallWindow", "50")
                .param("bigWindow", "50")
                .param("saveToFile", "true");
        assertBadRequestError(requestBuilder, "figi is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenIntervalIsMissing() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", TestShares.APPLE.getFigi())
                .param("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name())
                .param("movingAverageType", MovingAverageType.LINEAR_WEIGHTED.getValue())
                .param("smallWindow", "50")
                .param("bigWindow", "50")
                .param("saveToFile", "true");
        assertBadRequestError(requestBuilder, "from and to can't be both null");
    }

    @Test
    void getCandles_returnsBadRequest_whenCandleIntervalIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", TestShares.APPLE.getFigi())
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("movingAverageType", MovingAverageType.LINEAR_WEIGHTED.getValue())
                .param("smallWindow", "50")
                .param("bigWindow", "50")
                .param("saveToFile", "true");
        assertBadRequestError(requestBuilder, "candleInterval is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenMovingAverageTypeIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", TestShares.APPLE.getFigi())
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name())
                .param("smallWindow", "50")
                .param("bigWindow", "50")
                .param("saveToFile", "true");
        assertBadRequestError(requestBuilder, "movingAverageType is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenSmallWindowIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", TestShares.APPLE.getFigi())
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name())
                .param("movingAverageType", MovingAverageType.LINEAR_WEIGHTED.getValue())
                .param("bigWindow", "50")
                .param("saveToFile", "true");
        assertBadRequestError(requestBuilder, "smallWindow is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenBigWindowIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", TestShares.APPLE.getFigi())
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name())
                .param("movingAverageType", MovingAverageType.LINEAR_WEIGHTED.getValue())
                .param("smallWindow", "50")
                .param("saveToFile", "true");
        assertBadRequestError(requestBuilder, "bigWindow is mandatory");
    }

    @Test
    @DirtiesContext
    void getCandles_returnsCandles_whenParamsAreValid() throws Exception {
        final TestInstrument instrument = TestInstruments.APPLE;

        final String figi = instrument.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        Mocker.mockInstrument(instrumentsService, instrument);

        final HistoricCandle candle1 = new HistoricCandleBuilder()
                .setOpen(12000)
                .setClose(8000)
                .setHigh(15000)
                .setLow(6000)
                .setTime(DateTimeTestData.newDateTime(2021, 3, 25, 10))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle2 = new HistoricCandleBuilder()
                .setOpen(1200)
                .setClose(800)
                .setHigh(1500)
                .setLow(600)
                .setTime(DateTimeTestData.newDateTime(2021, 3, 25, 10, 1))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle3 = new HistoricCandleBuilder()
                .setOpen(120)
                .setClose(80)
                .setHigh(150)
                .setLow(60)
                .setTime(DateTimeTestData.newDateTime(2021, 3, 25, 10, 2))
                .setIsComplete(true)
                .build();

        final List<HistoricCandle> historicCandles = List.of(candle1, candle2, candle3);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final List<BigDecimal> shortAverages = TestData.newBigDecimalList(12000, 1200, 120);
        final List<BigDecimal> longAverages = TestData.newBigDecimalList(12000, 6600, 660);
        final List<Candle> candles = historicCandles.stream().map(CANDLE_MAPPER::map).toList();
        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, shortAverages, longAverages);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", figi)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", MovingAverageType.SIMPLE.getValue())
                .param("smallWindow", "1")
                .param("bigWindow", "2")
                .param("saveToFile", "true")
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, expectedResponse);
    }

    @Test
    @DirtiesContext
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final TestInstrument instrument = TestInstruments.APPLE;

        final String figi = instrument.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        Mocker.mockInstrument(instrumentsService, instrument);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", figi)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", MovingAverageType.SIMPLE.getValue())
                .param("smallWindow", "1")
                .param("bigWindow", "2")
                .param("saveToFile", "true")
                .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_catchesRuntimeException_whenSaveToFileTrue() throws Exception {
        final TestInstrument instrument = TestInstruments.APPLE;

        final String figi = instrument.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        Mocker.mockInstrument(instrumentsService, instrument);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", figi)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", MovingAverageType.SIMPLE.getValue())
                .param("smallWindow", "1")
                .param("bigWindow", "2")
                .param("saveToFile", "true")
                .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Mockito.doThrow(new RuntimeException())
                .when(excelService)
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileFalse() throws Exception {
        final TestInstrument instrument = TestInstruments.APPLE;

        final String figi = instrument.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        Mocker.mockInstrument(instrumentsService, instrument);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", figi)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", MovingAverageType.SIMPLE.getValue())
                .param("smallWindow", "1")
                .param("bigWindow", "2")
                .param("saveToFile", "false")
                .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileIsMissing() throws Exception {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();

        Mocker.mockInstrument(instrumentsService, instrument);

        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("figi", figi)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .param("candleInterval", CandleInterval.CANDLE_INTERVAL_1_MIN.name())
                .param("movingAverageType", MovingAverageType.SIMPLE.getValue())
                .param("smallWindow", "1")
                .param("bigWindow", "2")
                .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    // endregion

    @Test
    @DirtiesContext
    void getIndexWeights() throws Exception {
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

        final String figiesString = sharesLastPrices.keySet().stream().map(share -> '"' + share.getFigi() + '"').collect(Collectors.joining(","));
        final String requestString = "{\"figies\":[" + figiesString + "]}";
        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/capitalization-weights")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON);

        final SequencedMap<String, BigDecimal> expectedResponse = new LinkedHashMap<>();
        expectedResponse.put(share1.getFigi(), DecimalUtils.setDefaultScale(0.978361221));
        expectedResponse.put(share2.getFigi(), DecimalUtils.setDefaultScale(0.018799306));
        expectedResponse.put(share3.getFigi(), DecimalUtils.setDefaultScale(0.002839473));

        assertResponse(requestBuilder, expectedResponse);
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
    ) throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2024, 6, 1);
        Mocker.mockCurrency(instrumentsService, marketDataService, TestCurrencies.USD);
        Mocker.mockAllCurrencies(instrumentsService, TestCurrencies.USD, TestCurrencies.RUB);
        Mocker.mockAllShares(instrumentsService, marketDataService, shares, mockedNow);

        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(filtrationOptions);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get("/trader/statistics/most-profitable-shares")
                            .content(requestString)
                            .contentType(MediaType.APPLICATION_JSON);

            final String expectedResponse = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResult);

            assertResponse(requestBuilder, expectedResponse);
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
                getArgumentsForGetWeightedShares_havingDividendsWithinDays2000_saveToFileIsTrue(),
                getArgumentsForGetWeightedShares_excludeFiltrationByRegularInvestingAnnualReturns_saveToFileIsFalse(),
                getArgumentsForGetWeightedShares_fullFiltration_saveToFileIsNull()
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_havingDividendsWithinDays2000_saveToFileIsTrue() {
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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, true, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_excludeFiltrationByRegularInvestingAnnualReturns_saveToFileIsFalse() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS
                .withFilterByRegularInvestingAnnualReturns(false);

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

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, false, expectedResult);
    }

    private static Arguments getArgumentsForGetWeightedShares_fullFiltration_saveToFileIsNull() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final Map<String, Portfolio> accountsToPortfolios = getAccountsToPortfoliosArgument();

        final SharesFiltrationOptions filtrationOptions = TestData.BASIC_SHARE_FILTRATION_OPTIONS;

        final WeightedShare weightedShareBspb = TestData.newWeightedShare(
                TestShares.SPB_BANK,
                accountsToPortfolios,
                1,
                1,
                1,
                0
        );

        final List<WeightedShare> expectedResult = List.of(weightedShareBspb);

        return Arguments.of(accountsToPortfolios, shares, filtrationOptions, null, expectedResult);
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
            final Boolean saveToFile,
            final List<WeightedShare> expectedResult
    ) throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2024, 6, 1);
        Mocker.mockCurrency(instrumentsService, marketDataService, TestCurrencies.USD);
        Mocker.mockAllCurrencies(instrumentsService, TestCurrencies.USD, TestCurrencies.RUB);
        Mocker.mockAllShares(instrumentsService, marketDataService, shares, mockedNow);
        Mocker.mockPortfolios(operationsService, accountsToPortfolios);
        final List<String> figies = expectedResult.stream().map(WeightedShare::getFigi).toList();
        Mocker.mockLastPrices(marketDataService, accountsToPortfolios.values(), figies);

        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(filtrationOptions);

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            MockHttpServletRequestBuilder requestBuilder =
                    MockMvcRequestBuilders.get("/trader/statistics/weighted-shares")
                            .param("accountIds", String.join(",", accountsToPortfolios.keySet()))
                            .content(requestString)
                            .contentType(MediaType.APPLICATION_JSON);
            if (saveToFile != null) {
                requestBuilder = requestBuilder.param("saveToFile", String.valueOf(saveToFile));
            }
            final String expectedResponse = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResult);

            assertResponse(requestBuilder, expectedResponse);
            assertSavingToFile(saveToFile, expectedResult);
        }
    }

    private void assertSavingToFile(final Boolean saveToFile, final List<WeightedShare> expectedResult) {
        if (BooleanUtils.isTrue(saveToFile)) {
            Mockito.verify(excelService, Mockito.times(1))
                    .saveWeightedShares(Mockito.eq(expectedResult));
        } else {
            Mockito.verify(excelService, Mockito.never()).saveWeightedShares(Mockito.any());
        }
    }

    // endregion

}