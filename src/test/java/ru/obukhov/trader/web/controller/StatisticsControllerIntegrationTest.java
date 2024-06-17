package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
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
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Instrument;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.stream.Collectors;

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
        final Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();

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
        final TestInstrument testInstrument = TestInstruments.APPLE;

        final String figi = testInstrument.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

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
        final Instrument share = TestInstruments.APPLE.tinkoffInstrument();

        final String figi = share.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 3, 25, 19);

        Mocker.mockInstrument(instrumentsService, share);

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
        final Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();

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
        final Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
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

}