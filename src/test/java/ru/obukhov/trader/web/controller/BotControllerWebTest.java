package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.ScheduledBotConfig;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.test.utils.matchers.BigDecimalMatcher;
import ru.obukhov.trader.test.utils.matchers.CronExpressionMatcher;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.simulation.interfaces.Simulator;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.obukhov.trader.web.model.pojo.SimulatedPosition;
import ru.obukhov.trader.web.model.pojo.SimulationResult;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class BotControllerWebTest extends ControllerWebTest {

    @MockBean
    private Simulator simulator;

    @Autowired
    private ScheduledBotConfig scheduledBotConfig;

    @Test
    void simulate_returnsSimulationResults() throws Exception {
        final String ticker = "ticker";

        final String request = ResourceUtils.getResourceAsString("test-data/SimulateRequest.json");

        final List<SimulationResult> simulationResults = new ArrayList<>();
        final OffsetDateTime from = DateUtils.getDateTime(2021, 1, 1, 10, 0, 0);
        final OffsetDateTime to = DateUtils.getDateTime(2021, 2, 1, 0, 0, 0);
        final Interval interval = Interval.of(from, to);

        final SimulatedOperation operation = new SimulatedOperation(
                ticker,
                from,
                OperationType.BUY,
                BigDecimal.valueOf(10000),
                1,
                BigDecimal.valueOf(300)
        );
        final Candle candle = TestDataHelper.createCandle(
                10000, 20000, 30000, 5000, from, CandleResolution.DAY
        );

        final BigDecimal initialBalance = BigDecimal.valueOf(1000);
        final BigDecimal balanceIncrement = BigDecimal.valueOf(100);
        final CronExpression balanceIncrementCron = new CronExpression("0 0 0 1 * ?");
        final BotConfig botConfig1 = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult simulationResult1 = SimulationResult.builder()
                .botConfig(botConfig1)
                .interval(interval)
                .initialBalance(initialBalance)
                .totalInvestment(BigDecimal.valueOf(1000))
                .weightedAverageInvestment(BigDecimal.valueOf(1000))
                .finalTotalBalance(BigDecimal.valueOf(2000))
                .finalBalance(BigDecimal.valueOf(2000))
                .absoluteProfit(BigDecimal.valueOf(1000))
                .relativeProfit(1.0)
                .relativeYearProfit(12.0)
                .operations(List.of(operation))
                .candles(List.of(candle))
                .build();

        final BotConfig botConfig2 = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));
        final SimulatedPosition simulatedPosition1 = new SimulatedPosition(ticker, BigDecimal.valueOf(100000), 10);
        final SimulationResult simulationResult2 = SimulationResult.builder()
                .botConfig(botConfig2)
                .interval(interval)
                .initialBalance(initialBalance)
                .totalInvestment(BigDecimal.valueOf(10000))
                .weightedAverageInvestment(BigDecimal.valueOf(10000))
                .finalTotalBalance(BigDecimal.valueOf(20000))
                .finalBalance(BigDecimal.valueOf(10000))
                .absoluteProfit(BigDecimal.valueOf(1000))
                .relativeProfit(1.0)
                .relativeYearProfit(12.0)
                .positions(List.of(simulatedPosition1))
                .build();

        final BotConfig botConfig3 = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.LINEAR_GOLDEN_CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));
        final SimulatedPosition simulatedPosition2 = new SimulatedPosition(ticker, BigDecimal.valueOf(100), 10);
        final SimulatedPosition simulatedPosition3 = new SimulatedPosition(ticker, BigDecimal.valueOf(1000), 1);
        final SimulationResult simulationResult3 = SimulationResult.builder()
                .botConfig(botConfig3)
                .interval(interval)
                .initialBalance(initialBalance)
                .totalInvestment(BigDecimal.valueOf(2000))
                .weightedAverageInvestment(BigDecimal.valueOf(2000))
                .finalTotalBalance(BigDecimal.valueOf(4000))
                .finalBalance(BigDecimal.valueOf(3000))
                .absoluteProfit(BigDecimal.valueOf(1000))
                .relativeProfit(0.33)
                .relativeYearProfit(4.0)
                .positions(List.of(simulatedPosition2, simulatedPosition3))
                .error("error")
                .build();

        simulationResults.add(simulationResult1);
        simulationResults.add(simulationResult2);
        simulationResults.add(simulationResult3);

        Mockito.when(
                simulator.simulate(
                        Mockito.eq(ticker),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(initialBalance)),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement)),
                        ArgumentMatchers.argThat(CronExpressionMatcher.of(balanceIncrementCron)),
                        Mockito.anyList(),
                        Mockito.any(Interval.class),
                        Mockito.eq(true)
                )
        ).thenReturn(simulationResults);

        final String expectedResponse = ResourceUtils.getResourceAsString("test-data/SimulateResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/simulate")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(simulator, Mockito.times(1))
                .simulate(
                        Mockito.eq(ticker),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(initialBalance)),
                        ArgumentMatchers.argThat(BigDecimalMatcher.of(balanceIncrement)),
                        ArgumentMatchers.argThat(CronExpressionMatcher.of(balanceIncrementCron)),
                        Mockito.anyList(),
                        Mockito.any(Interval.class),
                        Mockito.eq(true)
                );
    }

    @Test
    void enableScheduling_enablesScheduling() throws Exception {
        scheduledBotConfig.setEnabled(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/enable")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assertions.assertTrue(scheduledBotConfig.isEnabled());
    }

    @Test
    void disableScheduling_disablesScheduling() throws Exception {
        scheduledBotConfig.setEnabled(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/disable")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assertions.assertFalse(scheduledBotConfig.isEnabled());
    }

    @Test
    void setTickers_setsTickers() throws Exception {
        scheduledBotConfig.setTickers(Set.of());

        final String tickers = ResourceUtils.getResourceAsString("test-data/SetTickersRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/tickers")
                .content(tickers)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        final Set<String> tickersList = scheduledBotConfig.getTickers();
        Assertions.assertEquals(3, tickersList.size());
        Assertions.assertTrue(tickersList.contains("ticker1"));
        Assertions.assertTrue(tickersList.contains("ticker2"));
        Assertions.assertTrue(tickersList.contains("ticker3"));
    }

}