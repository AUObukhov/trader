package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.CandleInterval;
import ru.obukhov.trader.market.model.Candles;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.exchange.CandlesResponse;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

class StatisticsControllerIntegrationTest extends ControllerIntegrationTest {

    @MockBean
    private ExcelService excelService;

    // region getCandles tests

    @Test
    void getCandles_returnsBadRequest_whenTickerIsMissing() throws Exception {
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.getValue())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);
        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getCandles_returnsBadRequest_whenFromIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.getValue())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'from' for method parameter type OffsetDateTime is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getCandles_returnsBadRequest_whenToIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("candleInterval", candleInterval.getValue())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'to' for method parameter type OffsetDateTime is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getCandles_returnsBadRequest_whenCandleIntervalIsMissing() throws Exception {
        final String ticker = "ticker";
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
    void getCandles_returnsBadRequest_whenMovingAverageTypeIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleInterval", candleInterval.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'movingAverageType' for method parameter type MovingAverageType is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getCandles_returnsBadRequest_whenSmallWindowIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.getValue())
                .param("movingAverageType", movingAverageType.getValue())
                .param("bigWindow", Integer.toString(bigWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'smallWindow' for method parameter type Integer is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    void getCandles_returnsBadRequest_whenBigWindowIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/statistics/candles")
                .param("ticker", ticker)
                .param("from", "2021-03-25T10:00:00+03:00")
                .param("to", "2021-03-25T19:00:00+03:00")
                .param("candleInterval", candleInterval.getValue())
                .param("movingAverageType", movingAverageType.getValue())
                .param("smallWindow", Integer.toString(smallWindow))
                .param("saveToFile", Boolean.TRUE.toString())
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'bigWindow' for method parameter type Integer is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getCandles_returnsCandles_whenParamsAreValid() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        mockFigiByTicker(ticker, figi);

        final Candle candle1 = TestData.createCandle(
                12000,
                8000,
                15000,
                6000,
                DateTimeTestData.createDateTime(2021, 3, 25, 10),
                candleInterval
        );

        final Candle candle2 = TestData.createCandle(
                1200,
                800,
                1500,
                600,
                DateTimeTestData.createDateTime(2021, 3, 25, 10, 1),
                candleInterval
        );

        final Candle candle3 = TestData.createCandle(
                120,
                80,
                150,
                60,
                DateTimeTestData.createDateTime(2021, 3, 25, 10, 2),
                candleInterval
        );
        final List<Candle> candles = List.of(candle1, candle2, candle3);
        mockCandles(figi, DateUtils.atStartOfDay(from), DateUtils.atEndOfDay(to), candleInterval, candles);

        final List<BigDecimal> shortAverages = TestData.createBigDecimalsList(12000, 1200, 120);
        final List<BigDecimal> longAverages = TestData.createBigDecimalsList(12000, 6600, 660);
        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, shortAverages, longAverages);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, expectedResponse);
    }

    @Test
    @DirtiesContext
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        mockFigiByTicker(ticker, figi);

        final List<Candle> candles = Collections.emptyList();
        mockCandles(figi, DateUtils.atStartOfDay(from), DateUtils.atEndOfDay(to), candleInterval, candles);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_catchesRuntimeException_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        mockFigiByTicker(ticker, figi);

        final List<Candle> candles = Collections.emptyList();
        mockCandles(figi, DateUtils.atStartOfDay(from), DateUtils.atEndOfDay(to), candleInterval, candles);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, Collections.emptyList(), Collections.emptyList());
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
        final String ticker = "ticker";
        final String figi = "figi";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        mockFigiByTicker(ticker, figi);

        final List<Candle> candles = Collections.emptyList();
        mockCandles(figi, DateUtils.atStartOfDay(from), DateUtils.atEndOfDay(to), candleInterval, candles);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("candleInterval", candleInterval.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.FALSE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileIsMissing() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";
        final CandleInterval candleInterval = CandleInterval._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final int smallWindow = 1;
        final int bigWindow = 2;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        mockFigiByTicker(ticker, figi);

        final List<Candle> candles = Collections.emptyList();
        mockCandles(figi, DateUtils.atStartOfDay(from), DateUtils.atEndOfDay(to), candleInterval, candles);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("candleInterval", candleInterval.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .contentType(MediaType.APPLICATION_JSON);

        final GetCandlesResponse expectedResponse = new GetCandlesResponse(candles, Collections.emptyList(), Collections.emptyList());

        performAndExpectResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    // endregion

    private void mockCandles(
            final String figi,
            final OffsetDateTime from,
            final OffsetDateTime to,
            final CandleInterval candleInterval,
            final List<Candle> candles
    ) throws JsonProcessingException {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/market/candles")
                .withQueryStringParameter("figi", figi)
                .withQueryStringParameter("from", from.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .withQueryStringParameter("to", to.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .withQueryStringParameter("interval", candleInterval.toString());

        final CandlesResponse candlesResponse = new CandlesResponse();
        final Candles payload = new Candles();
        payload.setCandles(candles);
        candlesResponse.setPayload(payload);
        mockResponse(apiRequest, candlesResponse);
    }

}