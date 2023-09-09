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
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.exchange.GetCandlesRequest;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class StatisticsControllerIntegrationTest extends ControllerIntegrationTest {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);

    @MockBean
    private ExcelService excelService;

    // region getCandles tests

    @Test
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

        assertGetBadRequestError("/trader/statistics/candles", request, "figi is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenIntervalIsMissing() throws Exception {
        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        assertGetBadRequestError("/trader/statistics/candles", request, "interval is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenCandleIntervalIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setInterval(Interval.of(from, to));
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        assertGetBadRequestError("/trader/statistics/candles", request, "candleInterval is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenMovingAverageTypeIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setSmallWindow(50);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        assertGetBadRequestError("/trader/statistics/candles", request, "movingAverageType is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenSmallWindowIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setBigWindow(50);
        request.setSaveToFile(true);

        assertGetBadRequestError("/trader/statistics/candles", request, "smallWindow is mandatory");
    }

    @Test
    void getCandles_returnsBadRequest_whenBigWindowIsMissing() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        final GetCandlesRequest request = new GetCandlesRequest();
        request.setFigi(TestShares.APPLE.share().figi());
        request.setInterval(Interval.of(from, to));
        request.setCandleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN);
        request.setMovingAverageType(MovingAverageType.LINEAR_WEIGHTED);
        request.setSmallWindow(50);
        request.setSaveToFile(true);

        assertGetBadRequestError("/trader/statistics/candles", request, "bigWindow is mandatory");
    }

    @Test
    @DirtiesContext
    void getCandles_returnsCandles_whenParamsAreValid() throws Exception {
        final Share share = TestShares.APPLE.tinkoffShare();

        final String figi = share.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);


        Mocker.mockShare(instrumentsService, share);

        final HistoricCandle candle1 = new HistoricCandleBuilder()
                .setOpen(12000)
                .setClose(8000)
                .setHigh(15000)
                .setLow(6000)
                .setTime(DateTimeTestData.createDateTime(2021, 3, 25, 10))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle2 = new HistoricCandleBuilder()
                .setOpen(1200)
                .setClose(800)
                .setHigh(1500)
                .setLow(600)
                .setTime(DateTimeTestData.createDateTime(2021, 3, 25, 10, 1))
                .setIsComplete(true)
                .build();

        final HistoricCandle candle3 = new HistoricCandleBuilder()
                .setOpen(120)
                .setClose(80)
                .setHigh(150)
                .setLow(60)
                .setTime(DateTimeTestData.createDateTime(2021, 3, 25, 10, 2))
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

        assertResponse(requestBuilder, expectedResponse);
    }

    @Test
    @DirtiesContext
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final TestShare testShare = TestShares.APPLE;

        final String figi = testShare.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

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

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_catchesRuntimeException_whenSaveToFileTrue() throws Exception {
        final Share share = TestShares.APPLE.tinkoffShare();

        final String figi = share.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, share);

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

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileFalse() throws Exception {
        final Share share = TestShares.APPLE.tinkoffShare();

        final String figi = share.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 3, 25, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 3, 25, 19);

        Mocker.mockShare(instrumentsService, share);

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

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    @Test
    @DirtiesContext
    void getCandles_doesNotCallSaveToFile_whenSaveToFileIsMissing() throws Exception {
        final Share share = TestShares.APPLE.tinkoffShare();

        final String figi = share.getFigi();

        Mocker.mockShare(instrumentsService, share);

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

        assertResponse(requestBuilder, expectedResponse);

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(figi), Mockito.any(Interval.class), Mockito.eq(expectedResponse));
    }

    // endregion

    @Test
    @DirtiesContext
    void getIndexWeights() throws Exception {
        final Share share1 = TestShares.APPLE.tinkoffShare();
        final Share share2 = TestShares.SBER.tinkoffShare();
        final Share share3 = TestShares.YANDEX.tinkoffShare();

        final Map<Share, Double> sharesLastPrices = new LinkedHashMap<>(3, 1);
        sharesLastPrices.put(share1, 178.7);
        sharesLastPrices.put(share2, 258.79);
        sharesLastPrices.put(share3, 2585.6);
        Mocker.mockSharesLastPrices(instrumentsService, marketDataService, sharesLastPrices);

        final Map<ru.tinkoff.piapi.contract.v1.Currency, Double> currenciesLastPrices = new LinkedHashMap<>(2, 1);
        currenciesLastPrices.put(TestCurrencies.USD.tinkoffCurrency(), 98.4225);
        currenciesLastPrices.put(TestCurrencies.RUB.tinkoffCurrency(), 1.0);
        Mocker.mockCurrenciesLastPrices(instrumentsService, marketDataService, currenciesLastPrices);

        final String figiesString = sharesLastPrices.keySet().stream().map(share -> '"' + share.getFigi() + '"').collect(Collectors.joining(","));
        final String requestString = "{\"shareFigies\":[" + figiesString + "]}";
        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/capitalization-weights")
                        .content(requestString)
                        .contentType(MediaType.APPLICATION_JSON);

        final Map<String, BigDecimal> expectedResponse = new LinkedHashMap<>();
        expectedResponse.put(share1.getFigi(), DecimalUtils.setDefaultScale(0.978361221));
        expectedResponse.put(share2.getFigi(), DecimalUtils.setDefaultScale(0.018799306));
        expectedResponse.put(share3.getFigi(), DecimalUtils.setDefaultScale(0.002839473));

        assertResponse(requestBuilder, expectedResponse);
    }

}