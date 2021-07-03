package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.TickerType;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class StatisticsControllerWebTest extends ControllerWebTest {

    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private ExcelService excelService;

    // region getCandles tests

    @Test
    void getCandles_returnsBadRequest_whenTickerIsMissing() throws Exception {
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher("Required String parameter 'ticker' is not present"))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenFromIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher("Required OffsetDateTime parameter 'from' is not present"))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenToIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher("Required OffsetDateTime parameter 'to' is not present"))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenCandleResolutionIsMissing() throws Exception {
        final String ticker = "ticker";

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher("Required CandleResolution parameter 'candleResolution' is not present"))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsCandles() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final Candle candle1 = TestDataHelper.createCandle(
                12000,
                8000,
                15000,
                6000,
                DateUtils.getDateTime(2021, 3, 25, 10, 0, 0),
                candleResolution
        );

        final Candle candle2 = TestDataHelper.createCandle(
                1200,
                800,
                1500,
                600,
                DateUtils.getDateTime(2021, 3, 25, 10, 1, 0),
                candleResolution
        );

        final Candle candle3 = TestDataHelper.createCandle(
                120,
                80,
                150,
                60,
                DateUtils.getDateTime(2021, 3, 25, 10, 2, 0),
                candleResolution
        );

        final List<BigDecimal> averages = List.of(
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100)
        );

        final List<Point> localMinimums = List.of(TestDataHelper.createPoint(candle1));
        final List<Point> localMaximums = List.of(TestDataHelper.createPoint(candle3));

        final List<Point> supportLine1 = List.of(
                Point.of(candle1.getTime(), 9000),
                Point.of(candle1.getTime(), 900)
        );
        final List<Point> supportLine2 = List.of(
                Point.of(candle1.getTime(), 900),
                Point.of(candle1.getTime(), 90)
        );
        final List<List<Point>> supportLines = List.of(supportLine1, supportLine2);

        final List<Point> resistanceLine1 = List.of(
                Point.of(candle1.getTime(), 11000),
                Point.of(candle1.getTime(), 1100)
        );
        final List<Point> resistanceLine2 = List.of(
                Point.of(candle1.getTime(), 1100),
                Point.of(candle1.getTime(), 110)
        );
        final List<List<Point>> resistanceLines = List.of(resistanceLine1, resistanceLine2);

        final GetCandlesResponse response = new GetCandlesResponse(
                List.of(candle1, candle2, candle3),
                averages,
                localMinimums,
                localMaximums,
                supportLines,
                resistanceLines
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution)
        )).thenReturn(response);

        final String expectedResponse = ResourceUtils.getTestDataAsString("GetCandlesResponse.json");

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(statisticsService, Mockito.times(1))
                .getExtendedCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution));
    }

    @Test
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution)
        )).thenReturn(response);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));
    }

    @Test
    void getCandles_catchesRuntimeException_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution)
        )).thenReturn(response);

        Mockito.doThrow(new RuntimeException())
                .when(excelService)
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));
    }

    @Test
    void getCandles_doesNotCallSaveToFile_whenSaveToFileFalse() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution)
        )).thenReturn(response);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("saveToFile", Boolean.FALSE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));
    }

    @Test
    void getCandles_doesNotCallSaveToFile_whenSaveToFileIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(candleResolution)
        )).thenReturn(response);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));
    }

    // endregion

    @Test
    void getInstruments() throws Exception {
        final String getCandlesRequest = ResourceUtils.getTestDataAsString("GetInstrumentsRequest.json");
        final String expectedResponse = ResourceUtils.getTestDataAsString("GetInstrumentsResponse.json");

        MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(InstrumentType.STOCK);
        MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(InstrumentType.BOND);

        Mockito.when(statisticsService.getInstruments(TickerType.STOCK))
                .thenReturn(List.of(instrument1, instrument2));

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/instruments")
                .content(getCandlesRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(statisticsService, Mockito.times(1))
                .getInstruments(TickerType.STOCK);
    }

}