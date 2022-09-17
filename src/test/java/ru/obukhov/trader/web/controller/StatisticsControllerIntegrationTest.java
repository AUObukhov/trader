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
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

class StatisticsControllerIntegrationTest extends ControllerIntegrationTest {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);

    @MockBean
    private ExcelService excelService;

    // region getCandles tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenTickerIsMissing() throws Exception {
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);
        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenFromIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'from' for method parameter type OffsetDateTime is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenToIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'to' for method parameter type OffsetDateTime is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenCandleIntervalIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'candleInterval' for method parameter type CandleInterval is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenMovingAverageTypeIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleInterval", candleInterval.name())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'movingAverageType' for method parameter type MovingAverageType is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenSmallWindowIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", movingAverageType.getValue())
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'smallWindow' for method parameter type Integer is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenBigWindowIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.name())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'bigWindow' for method parameter type Integer is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsCandles_whenParamsAreValid() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final HistoricCandle candle1 = new HistoricCandleBuilder()
                .setOpenPrice(12000)
                .setClosePrice(8000)
                .setHighestPrice(15000)
                .setLowestPrice(6000)
                .setTime(DateTimeTestData.createDateTime(2021, 3, 25, 10))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle2 = new HistoricCandleBuilder()
                .setOpenPrice(1200)
                .setClosePrice(800)
                .setHighestPrice(1500)
                .setLowestPrice(600)
                .setTime(DateTimeTestData.createDateTime(2021, 3, 25, 10, 1))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle3 = new HistoricCandleBuilder()
                .setOpenPrice(120)
                .setClosePrice(80)
                .setHighestPrice(150)
                .setLowestPrice(60)
                .setTime(DateTimeTestData.createDateTime(2021, 3, 25, 10, 2))
                .setIsComplete(true)
                .build();

        final List<HistoricCandle> historicCandles = List.of(candle1, candle2, candle3);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final List<BigDecimal> shortAverages = TestData.createBigDecimalsList(12000, 1200, 120);
        final List<BigDecimal> longAverages = TestData.createBigDecimalsList(12000, 6600, 660);
        final List<Candle> candles = historicCandles.stream().map(CANDLE_MAPPER::map).toList();
        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, shortAverages, longAverages);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.name())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, expectedResponse);
    }

    @Test
    @DirtiesContext
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.name())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_catchesRuntimeException_whenSaveToFileTrue() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.name())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Mockito.doThrow(new RuntimeException())
                .when(excelService)
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileFalse() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.name())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.FALSE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileIsMissing() throws Exception {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("candleInterval", candleInterval.name())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    // endregion

}