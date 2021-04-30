package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.bot.interfaces.Simulator;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BotControllerWebTest extends ControllerIntegrationTest {

    @Captor
    private ArgumentCaptor<List<String>> stringListArgumentCaptor;

    @MockBean
    private Simulator simulator;
    @MockBean
    private BotConfig botConfig;

    @Test
    void simulate_returnsSimulationResults() throws Exception {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";

        String request = ResourceUtils.getResourceAsString("test-data/SimulateRequest.json");

        Map<String, List<SimulationResult>> simulationResultsMap = new HashMap<>();
        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 2, 1, 0, 0, 0);
        final Interval interval = Interval.of(from, to);
        SimulatedPosition simulatedPosition1 = new SimulatedPosition();
        simulatedPosition1.setTicker(ticker1);
        simulatedPosition1.setPrice(BigDecimal.valueOf(100000));
        simulatedPosition1.setQuantity(10);

        SimulatedOperation operation = new SimulatedOperation(
                ticker1,
                from,
                OperationType.BUY,
                BigDecimal.valueOf(10000),
                1,
                BigDecimal.valueOf(300)
        );
        Candle candle = TestDataHelper.createCandle(
                10000, 20000, 30000, 5000, from, CandleResolution.DAY
        );

        SimulationResult simulationResult1 = SimulationResult.builder()
                .botName("ticker1 bot1")
                .interval(interval)
                .initialBalance(BigDecimal.valueOf(100000))
                .totalInvestment(BigDecimal.valueOf(100000))
                .weightedAverageInvestment(BigDecimal.valueOf(100000))
                .finalTotalBalance(BigDecimal.valueOf(200000))
                .finalBalance(BigDecimal.valueOf(200000))
                .absoluteProfit(BigDecimal.valueOf(100000))
                .relativeProfit(1.0)
                .relativeYearProfit(12.0)
                .operations(List.of(operation))
                .candles(List.of(candle))
                .build();
        SimulationResult simulationResult2 = SimulationResult.builder()
                .botName("ticker1 bot2")
                .interval(interval)
                .initialBalance(BigDecimal.valueOf(1000000))
                .totalInvestment(BigDecimal.valueOf(1000000))
                .weightedAverageInvestment(BigDecimal.valueOf(1000000))
                .finalTotalBalance(BigDecimal.valueOf(2000000))
                .finalBalance(BigDecimal.valueOf(1000000))
                .absoluteProfit(BigDecimal.valueOf(100000))
                .relativeProfit(1.0)
                .relativeYearProfit(12.0)
                .positions(List.of(simulatedPosition1))
                .build();
        simulationResultsMap.put(ticker1, List.of(simulationResult1, simulationResult2));

        SimulatedPosition simulatedPosition2 = new SimulatedPosition();
        simulatedPosition2.setTicker(ticker2);
        simulatedPosition2.setPrice(BigDecimal.valueOf(10000));
        simulatedPosition2.setQuantity(10);
        SimulatedPosition simulatedPosition3 = new SimulatedPosition();
        simulatedPosition3.setTicker(ticker2);
        simulatedPosition3.setPrice(BigDecimal.valueOf(100000));
        simulatedPosition3.setQuantity(1);
        SimulationResult simulationResult3 = SimulationResult.builder()
                .botName("ticker2 bot1")
                .interval(interval)
                .initialBalance(BigDecimal.valueOf(200000))
                .totalInvestment(BigDecimal.valueOf(200000))
                .weightedAverageInvestment(BigDecimal.valueOf(200000))
                .finalTotalBalance(BigDecimal.valueOf(400000))
                .finalBalance(BigDecimal.valueOf(300000))
                .absoluteProfit(BigDecimal.valueOf(100000))
                .relativeProfit(0.33)
                .relativeYearProfit(4.0)
                .positions(List.of(simulatedPosition2, simulatedPosition3))
                .error("error")
                .build();
        simulationResultsMap.put(ticker2, List.of(simulationResult3));

        Mockito.when(simulator.simulate(Mockito.anyList(), Mockito.any(Interval.class), Mockito.eq(true)))
                .thenReturn(simulationResultsMap);

        String expectedResponse = ResourceUtils.getResourceAsString("test-data/SimulateResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/simulate")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(simulator, Mockito.times(1))
                .simulate(Mockito.anyList(), Mockito.any(Interval.class), Mockito.eq(true));
    }

    @Test
    void enableScheduling_enablesScheduling() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/enable")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(botConfig, Mockito.times(1))
                .setEnabled(true);
    }

    @Test
    void disableScheduling_disablesScheduling() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/disable")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(botConfig, Mockito.times(1))
                .setEnabled(false);
    }

    @Test
    void setTickers_setsTickers() throws Exception {
        String tickers = ResourceUtils.getResourceAsString("test-data/SetTickersRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/tickers")
                .content(tickers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(botConfig, Mockito.times(1))
                .setTickers(stringListArgumentCaptor.capture());

        List<String> tickersList = stringListArgumentCaptor.getValue();
        Assertions.assertEquals(3, tickersList.size());
        Assertions.assertEquals("ticker1", tickersList.get(0));
        Assertions.assertEquals("ticker2", tickersList.get(1));
        Assertions.assertEquals("ticker3", tickersList.get(2));
    }

}