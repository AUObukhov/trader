package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.market.model.ExtendedCandle;
import ru.obukhov.trader.market.model.Extremum;
import ru.obukhov.trader.market.model.TickerType;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

class StatisticsControllerWebTest extends ControllerIntegrationTest {

    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private ExcelService excelService;

    // region getCandles tests

    @Test
    void getCandles_returnsCandles() throws Exception {
        final String ticker = "ticker";

        ExtendedCandle candle1 = new ExtendedCandle();
        candle1.setOpenPrice(BigDecimal.valueOf(12000));
        candle1.setClosePrice(BigDecimal.valueOf(8000));
        candle1.setHighestPrice(BigDecimal.valueOf(15000));
        candle1.setLowestPrice(BigDecimal.valueOf(6000));
        candle1.setTime(DateUtils.getDateTime(2021, 3, 25, 10, 0, 0));
        candle1.setInterval(CandleResolution._1MIN);
        candle1.setAveragePrice(BigDecimal.valueOf(10000));
        candle1.setExtremum(Extremum.MAX);
        candle1.setSupportValue(BigDecimal.valueOf(9000));
        candle1.setResistanceValue(BigDecimal.valueOf(11000));

        ExtendedCandle candle2 = new ExtendedCandle();
        candle2.setOpenPrice(BigDecimal.valueOf(1200));
        candle2.setClosePrice(BigDecimal.valueOf(800));
        candle2.setHighestPrice(BigDecimal.valueOf(1500));
        candle2.setLowestPrice(BigDecimal.valueOf(600));
        candle2.setTime(DateUtils.getDateTime(2021, 3, 25, 10, 1, 0));
        candle2.setInterval(CandleResolution._1MIN);
        candle2.setAveragePrice(BigDecimal.valueOf(1000));
        candle2.setExtremum(Extremum.NONE);
        candle2.setSupportValue(BigDecimal.valueOf(900));
        candle2.setResistanceValue(BigDecimal.valueOf(1100));

        ExtendedCandle candle3 = new ExtendedCandle();
        candle3.setOpenPrice(BigDecimal.valueOf(120));
        candle3.setClosePrice(BigDecimal.valueOf(80));
        candle3.setHighestPrice(BigDecimal.valueOf(150));
        candle3.setLowestPrice(BigDecimal.valueOf(60));
        candle3.setTime(DateUtils.getDateTime(2021, 3, 25, 10, 2, 0));
        candle3.setInterval(CandleResolution._1MIN);
        candle3.setAveragePrice(BigDecimal.valueOf(100));
        candle3.setExtremum(Extremum.MIN);
        candle3.setSupportValue(BigDecimal.valueOf(90));
        candle3.setResistanceValue(BigDecimal.valueOf(110));

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
        )).thenReturn(Arrays.asList(candle1, candle2, candle3));

        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetCandlesRequest1.json");
        String expectedResponse = ResourceUtils.getResourceAsString("test-data/GetCandlesResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/candles")
                .content(getCandlesRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(statisticsService, Mockito.times(1))
                .getExtendedCandles(
                        Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
                );
    }

    @Test
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
        )).thenReturn(Collections.emptyList());

        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetCandlesRequest1.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/candles")
                .content(getCandlesRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.times(1))
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.anyList());
    }

    @Test
    void getCandles_doesNotCallSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
        )).thenReturn(Collections.emptyList());

        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetCandlesRequest2.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/candles")
                .content(getCandlesRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.anyList());
    }

    // endregion

    @Test
    void getInstruments() throws Exception {
        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetInstrumentsRequest.json");
        String expectedResponse = ResourceUtils.getResourceAsString("test-data/GetInstrumentsResponse.json");

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
                .thenReturn(Arrays.asList(instrument1, instrument2));

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