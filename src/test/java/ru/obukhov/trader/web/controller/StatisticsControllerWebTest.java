package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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

import java.io.File;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

class StatisticsControllerWebTest extends ControllerIntegrationTest {

    @MockBean
    private StatisticsService statisticsService;
    @MockBean
    private ExcelService excelService;

    @Mock
    private File file;
    @Mock
    private Runtime runtime;

    // region getCandles tests

    @Test
    @SuppressWarnings("unused")
    void getCandles_returnsCandles() throws Exception {
        final String ticker = "ticker";

        Candle candle1 = TestDataHelper.createCandle(
                12000,
                8000,
                15000,
                6000,
                DateUtils.getDateTime(2021, 3, 25, 10, 0, 0),
                CandleResolution._1MIN
        );

        Candle candle2 = TestDataHelper.createCandle(
                1200,
                800,
                1500,
                600,
                DateUtils.getDateTime(2021, 3, 25, 10, 1, 0),
                CandleResolution._1MIN
        );

        Candle candle3 = TestDataHelper.createCandle(
                120,
                80,
                150,
                60,
                DateUtils.getDateTime(2021, 3, 25, 10, 2, 0),
                CandleResolution._1MIN
        );

        List<BigDecimal> averages = List.of(
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100)
        );

        List<Point> localMinimums = List.of(TestDataHelper.createPoint(candle1));
        List<Point> localMaximums = List.of(TestDataHelper.createPoint(candle3));

        List<Point> supportLine1 = List.of(
                Point.of(candle1.getTime(), 9000),
                Point.of(candle1.getTime(), 900)
        );
        List<Point> supportLine2 = List.of(
                Point.of(candle1.getTime(), 900),
                Point.of(candle1.getTime(), 90)
        );
        List<List<Point>> supportLines = List.of(supportLine1, supportLine2);

        List<Point> resistanceLine1 = List.of(
                Point.of(candle1.getTime(), 11000),
                Point.of(candle1.getTime(), 1100)
        );
        List<Point> resistanceLine2 = List.of(
                Point.of(candle1.getTime(), 1100),
                Point.of(candle1.getTime(), 110)
        );
        List<List<Point>> resistanceLines = List.of(resistanceLine1, resistanceLine2);

        GetCandlesResponse response = new GetCandlesResponse(
                List.of(candle1, candle2, candle3),
                averages,
                localMinimums,
                localMaximums,
                supportLines,
                resistanceLines
        );

        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
        )).thenReturn(response);

        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetCandlesRequest1.json");
        String expectedResponse = ResourceUtils.getResourceAsString("test-data/GetCandlesResponse.json");

        final String fileAbsolutePath = "file absolute path";
        mockFile(ticker, fileAbsolutePath);

        try (MockedStatic<Runtime> runtimeStaticMock = TestDataHelper.mockRuntime(runtime)) {
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
            Mockito.verify(runtime, Mockito.times(1))
                    .exec(new String[]{"explorer", fileAbsolutePath});
        }
    }

    @Test
    @SuppressWarnings("unused")
    void getCandles_callsSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";

        GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
        )).thenReturn(response);

        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetCandlesRequest1.json");

        final String fileAbsolutePath = "file absolute path";
        mockFile(ticker, fileAbsolutePath);

        try (MockedStatic<Runtime> runtimeStaticMock = TestDataHelper.mockRuntime(runtime)) {
            mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/candles")
                    .content(getCandlesRequest)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

            Mockito.verify(excelService, Mockito.times(1))
                    .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));
            Mockito.verify(runtime, Mockito.times(1))
                    .exec(new String[]{"explorer", fileAbsolutePath});
        }
    }

    @Test
    void getCandles_doesNotCallSaveToFile_whenSaveToFileTrue() throws Exception {
        final String ticker = "ticker";

        GetCandlesResponse response = new GetCandlesResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        Mockito.when(statisticsService.getExtendedCandles(
                Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(CandleResolution._1MIN)
        )).thenReturn(response);

        String getCandlesRequest = ResourceUtils.getResourceAsString("test-data/GetCandlesRequest2.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/statistics/candles")
                .content(getCandlesRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        Mockito.verify(excelService, Mockito.never())
                .saveCandles(Mockito.eq(ticker), Mockito.any(Interval.class), Mockito.eq(response));
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

    private void mockFile(String ticker, String fileAbsolutePath) {
        Mockito.when(
                excelService.saveCandles(
                        Mockito.eq(ticker),
                        Mockito.any(Interval.class),
                        Mockito.any(GetCandlesResponse.class)
                )
        ).thenReturn(file);
        Mockito.when(file.getAbsolutePath()).thenReturn(fileAbsolutePath);
    }

}