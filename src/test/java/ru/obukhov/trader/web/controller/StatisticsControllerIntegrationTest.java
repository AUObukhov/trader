package ru.obukhov.trader.web.controller;

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
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.SharesFiltrationFlags;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.ShareType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
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

    private static final SharesFiltrationFlags ALL_FILTRATION_FLAGS = new SharesFiltrationFlags(
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true
    );

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetMostProfitableShares() {
        return Stream.of(
                getArgumentsForGetMostProfitableShares_excludeFiltrationByCurrency(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByApiTradeAvailableFlag(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByForQualInvestorFlag(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByForIisFlag(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByShareType(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByTradingPeriod(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByHavingDividends(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByHavingRecentDividends(),
                getArgumentsForGetMostProfitableShares_excludeFiltrationByRegularInvestingAnnualReturns(),
                getArgumentsForGetMostProfitableShares_fullFiltration()
        );
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByCurrency() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.APPLE.withForQualInvestorFlag(false)
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS.withFilterByCurrency(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.APPLE.getName(), 0.055922813848445996);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByApiTradeAvailableFlag() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS.withFilterByApiTradeAvailableFlag(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);
        expectedResult.put(TestShares.TRANS_CONTAINER.getName(), 0.10967098669399622);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByForQualInvestorFlag() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.SELIGDAR.withForQualInvestorFlag(true)
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS.withFilterByForQualInvestorFlag(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SELIGDAR.getName(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByForIisFlag() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.SELIGDAR.withForIisFlag(false)
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS.withFilterByForIisFlag(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SELIGDAR.getName(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByShareType() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER,
                TestShares.SELIGDAR.withShareType(ShareType.SHARE_TYPE_ADR)
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS.withFilterByShareType(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SELIGDAR.getName(), 0.10300118574872186);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByTradingPeriod() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS.withFilterByTradingPeriod(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);
        expectedResult.put(TestShares.WOOSH.getName(), 0.1424311683598216);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByHavingDividends() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.PIK,
                TestShares.GAZPROM,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS
                .withFilterByHavingDividends(false)
                .withFilterByHavingRecentDividends(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.PIK.getName(), 0.057035507311362865);
        expectedResult.put(TestShares.RBC.getName(), 0.06186532835804104);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
    }

    private static Arguments getArgumentsForGetMostProfitableShares_excludeFiltrationByHavingRecentDividends() {
        final List<TestShare> shares = List.of(
                TestShares.SPB_BANK,
                TestShares.GAZPROM,
                TestShares.PIK,
                TestShares.RBC,
                TestShares.WOOSH,
                TestShares.TRANS_CONTAINER
        );

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS
                .withFilterByHavingRecentDividends(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.PIK.getName(), 0.057035507311362865);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
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

        final SharesFiltrationFlags filtrationFlags = ALL_FILTRATION_FLAGS
                .withFilterByRegularInvestingAnnualReturns(false);

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestShares.GAZPROM.getName(), 0.012762569152802827);
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, filtrationFlags, expectedResult);
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

        final SequencedMap<String, Double> expectedResult = new LinkedHashMap<>();
        expectedResult.put(TestCurrencies.USD.getName(), 0.019214449974637393);
        expectedResult.put(TestShares.SPB_BANK.getName(), 0.10691806625087197);

        return Arguments.of(shares, ALL_FILTRATION_FLAGS, expectedResult);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetMostProfitableShares")
    @DirtiesContext
    void getMostProfitableShares(
            final List<TestShare> shares,
            final SharesFiltrationFlags filtrationFlags,
            final SequencedMap<String, Double> expectedResult
    ) throws Exception {
        final OffsetDateTime mockedNow = DateTimeTestData.newDateTime(2024, 6, 1);
        Mocker.mockCurrency(instrumentsService, marketDataService, TestCurrencies.USD);
        Mocker.mockAllCurrencies(instrumentsService, TestCurrencies.USD, TestCurrencies.RUB);
        Mocker.mockAllShares(instrumentsService, marketDataService, shares, mockedNow);

        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(filtrationFlags);

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

}