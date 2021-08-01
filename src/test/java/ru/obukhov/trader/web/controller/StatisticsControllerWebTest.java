package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsResponse;
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
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);
        final String expectedMessage =
                "Required request parameter 'ticker' for method parameter type String is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenFromIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage =
                "Required request parameter 'from' for method parameter type OffsetDateTime is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenToIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage =
                "Required request parameter 'to' for method parameter type OffsetDateTime is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenCandleResolutionIsMissing() throws Exception {
        final String ticker = "ticker";
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage =
                "Required request parameter 'candleResolution' for method parameter type CandleResolution is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenMovingAverageTypeIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final int smallWindow = 50;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage =
                "Required request parameter 'movingAverageType' for method parameter type MovingAverageType is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenSmallWindowIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int bigWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("bigWindow", Integer.toString(bigWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage =
                "Required request parameter 'smallWindow' for method parameter type Integer is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsBadRequest_whenBigWindowIsMissing() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.LINEAR_WEIGHTED;
        final int smallWindow = 50;

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", Integer.toString(smallWindow))
                        .param("saveToFile", Boolean.TRUE.toString())
                        .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage =
                "Required request parameter 'bigWindow' for method parameter type Integer is not present";
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(getJsonPathMessageMatcher(expectedMessage))
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCandles_returnsCandles_whenParamsAreValid() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer smallWindow = 1;
        final Integer bigWindow = 2;

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

        final List<BigDecimal> shortAverages = List.of(
                BigDecimal.valueOf(12000),
                BigDecimal.valueOf(1200),
                BigDecimal.valueOf(120)
        );
        final List<BigDecimal> longAverages = List.of(
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100)
        );

        final GetCandlesResponse response = new GetCandlesResponse(
                List.of(candle1, candle2, candle3),
                shortAverages,
                longAverages
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker),
                Mockito.any(Interval.class),
                Mockito.eq(candleResolution),
                Mockito.eq(movingAverageType),
                Mockito.eq(smallWindow),
                Mockito.eq(bigWindow)
        )).thenReturn(response);

        final String expectedResponse = ResourceUtils.getTestDataAsString("GetCandlesResponse.json");

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", smallWindow.toString())
                        .param("bigWindow", bigWindow.toString())
                        .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    @Test
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";
        final CandleResolution candleResolution = CandleResolution._1MIN;
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer smallWindow = 1;
        final Integer bigWindow = 2;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker),
                Mockito.any(Interval.class),
                Mockito.eq(candleResolution),
                Mockito.eq(movingAverageType),
                Mockito.eq(smallWindow),
                Mockito.eq(bigWindow)
        )).thenReturn(response);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", smallWindow.toString())
                        .param("bigWindow", bigWindow.toString())
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
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer smallWindow = 1;
        final Integer bigWindow = 2;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker),
                Mockito.any(Interval.class),
                Mockito.eq(candleResolution),
                Mockito.eq(movingAverageType),
                Mockito.eq(smallWindow),
                Mockito.eq(bigWindow)
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
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", smallWindow.toString())
                        .param("bigWindow", bigWindow.toString())
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
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer smallWindow = 1;
        final Integer bigWindow = 2;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker),
                Mockito.any(Interval.class),
                Mockito.eq(candleResolution),
                Mockito.eq(movingAverageType),
                Mockito.eq(smallWindow),
                Mockito.eq(bigWindow)
        )).thenReturn(response);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("candleResolution", candleResolution.getValue())
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("smallWindow", smallWindow.toString())
                        .param("bigWindow", bigWindow.toString())
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
        final MovingAverageType movingAverageType = MovingAverageType.SIMPLE;
        final Integer smallWindow = 1;
        final Integer bigWindow = 2;

        final GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker),
                Mockito.any(Interval.class),
                Mockito.eq(candleResolution),
                Mockito.eq(movingAverageType),
                Mockito.eq(smallWindow),
                Mockito.eq(bigWindow)
        )).thenReturn(response);

        final MockHttpServletRequestBuilder requestBuilder =
                MockMvcRequestBuilders.get("/trader/statistics/candles")
                        .param("ticker", ticker)
                        .param("from", "2021-03-25T10:00:00+03:00")
                        .param("to", "2021-03-25T19:00:00+03:00")
                        .param("movingAverageType", movingAverageType.getValue())
                        .param("candleResolution", candleResolution.getValue())
                        .param("smallWindow", smallWindow.toString())
                        .param("bigWindow", bigWindow.toString())
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
        final InstrumentType instrumentType = InstrumentType.STOCK;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        Mockito.when(statisticsService.getInstruments(instrumentType))
                .thenReturn(instruments);

        final String expectedResponse = new ObjectMapper()
                .writeValueAsString(new GetInstrumentsResponse(instruments));

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/instruments")
                .param("instrumentType", instrumentType.getValue())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(statisticsService, Mockito.times(1))
                .getInstruments(instrumentType);
    }

}