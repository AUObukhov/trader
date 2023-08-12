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
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.web.model.exchange.GetCandlesRequest;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    void getCandles_returnsBadRequest_whenFigiIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        getAndExpectBadRequestError("/trader/statistics/candles", request, "figi is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenIntervalIsMissing() throws Exception {
        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShare1.FIGI);
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        getAndExpectBadRequestError("/trader/statistics/candles", request, "interval is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenCandleIntervalIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShare1.FIGI);
        request.setInterval(Interval.of(from, to));
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        getAndExpectBadRequestError("/trader/statistics/candles", request, "candleInterval is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenMovingAverageTypeIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShare1.FIGI);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        getAndExpectBadRequestError("/trader/statistics/candles", request, "movingAverageType is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenSmallWindowIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShare1.FIGI);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        getAndExpectBadRequestError("/trader/statistics/candles", request, "smallWindow is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsBadRequest_whenBigWindowIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShare1.FIGI);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setSaveToFile(true);

        getAndExpectBadRequestError("/trader/statistics/candles", request, "bigWindow is mandatory");
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCandles_returnsCandles_whenParamsAreValid() throws Exception {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, TestShare1.SHARE);

        final HistoricCandle candle1 = new HistoricCandleBuilder()
                .setOpen(12000)
                .setClose(8000)
                .setHigh(15000)
                .setLow(6000)
                .setTime(TimestampUtils.newTimestamp(2021, 3, 25, 10))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle2 = new HistoricCandleBuilder()
                .setOpen(1200)
                .setClose(800)
                .setHigh(1500)
                .setLow(600)
                .setTime(TimestampUtils.newTimestamp(2021, 3, 25, 10, 1))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle3 = new HistoricCandleBuilder()
                .setOpen(120)
                .setClose(80)
                .setHigh(150)
                .setLow(60)
                .setTime(TimestampUtils.newTimestamp(2021, 3, 25, 10, 2))
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

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(figi);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(candleInterval);
        request.setMovingAverageType(MovingAverageType.SIMPLE);
        request.setSmallWindow(1);
        request.setBigWindow(2);
        request.setSaveToFile(true);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .content(TestUtils.OBJECT_MAPPER.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, expectedResponse);
    }

    @Test
    @DirtiesContext
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, TestShare1.SHARE);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(figi);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(candleInterval);
        request.setMovingAverageType(MovingAverageType.SIMPLE);
        request.setSmallWindow(1);
        request.setBigWindow(2);
        request.setSaveToFile(true);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .content(TestUtils.OBJECT_MAPPER.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_catchesRuntimeException_whenSaveToFileTrue() throws Exception {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, TestShare1.SHARE);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(figi);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(candleInterval);
        request.setMovingAverageType(MovingAverageType.SIMPLE);
        request.setSmallWindow(1);
        request.setBigWindow(2);
        request.setSaveToFile(true);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .content(TestUtils.OBJECT_MAPPER.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Mockito.doThrow(new RuntimeException())
                .when(excelService)
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileFalse() throws Exception {
        final String figi = TestShare1.FIGI;
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, TestShare1.SHARE);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(figi);
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(candleInterval);
        request.setMovingAverageType(MovingAverageType.SIMPLE);
        request.setSmallWindow(1);
        request.setBigWindow(2);
        request.setSaveToFile(false);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .content(TestUtils.OBJECT_MAPPER.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileIsMissing() throws Exception {
        final String figi = TestShare1.FIGI;

        Mocker.mockShare(instrumentsService, TestShare1.SHARE);

        final String requestString = String.format("""
                {
                  "figi": "%s",
                  "interval": {
                    "from": "2021-03-25T10:00:00+03:00",
                    "to": "2021-03-25T19:00:00+03:00"
                  },
                  "candleInterval": "CANDLE_INTERVAL_1_MIN",
                  "movingAverageType": "SMA",
                  "smallWindow": 1,
                  "bigWindow": 2
                }""", figi);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    // endregion

}