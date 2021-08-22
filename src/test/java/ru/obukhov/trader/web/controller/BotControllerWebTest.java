package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.simulation.interfaces.Simulator;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.obukhov.trader.web.model.SimulatedPosition;
import ru.obukhov.trader.web.model.SimulationResult;
import ru.obukhov.trader.web.model.TradingConfig;
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
    private ScheduledBotProperties scheduledBotProperties;

    // region simulate tests

    @Test
    void simulate_returnsBadRequest_whenTickerIsNull() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_tickerIsNull.json",
                "ticker is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenTickerIsEmpty() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_tickerIsEmpty.json",
                "ticker is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenTickerIsBlank() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_tickerIsBlank.json",
                "ticker is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenFromIsNull() throws Exception {
        simulate_returnsBadRequest_withError("SimulateRequest/SimulateRequest_fromIsNull.json", "from is mandatory");
    }

    @Test
    void simulate_returnsBadRequest_whenBalanceConfigIsNull() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_balanceConfigsIsNull.json",
                "balanceConfig is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenTradingConfigsIsNull() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_tradingConfigsIsNull.json",
                "tradingConfigs is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenBotsConfigsIsEmpty() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_tradingConfigsIsEmpty.json",
                "tradingConfigs is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenCandleResolutionIsNull() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_candleResolutionIsNull.json",
                "candleResolution is mandatory"
        );
    }

    @Test
    void simulate_returnsBadRequest_whenStrategyTypeIsNull() throws Exception {
        simulate_returnsBadRequest_withError(
                "SimulateRequest/SimulateRequest_strategyTypeIsNull.json",
                "strategyType is mandatory"
        );
    }

    private void simulate_returnsBadRequest_withError(String simulateRequestResource, String expectedError) throws Exception {
        final String requestString = ResourceUtils.getTestDataAsString(simulateRequestResource);

        assertBadRequestError("/trader/bot/simulate", requestString, expectedError);
    }

    @Test
    void simulate_returnsSimulationResults_whenRequestIsValid() throws Exception {
        final String ticker = "ticker";

        final String request = ResourceUtils.getTestDataAsString("SimulateRequest/SimulateRequest_valid.json");

        final List<SimulationResult> simulationResults = new ArrayList<>();
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1);
        final Interval interval = Interval.of(from, to);

        final SimulatedOperation operation = new SimulatedOperation(
                ticker,
                from,
                OperationType.BUY,
                BigDecimal.valueOf(10000),
                1,
                BigDecimal.valueOf(300)
        );
        final Candle candle = TestData.createCandle(10000, 20000, 30000, 5000, from, CandleResolution.DAY);

        final BalanceConfig balanceConfig = new BalanceConfig(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(100),
                new CronExpression("0 0 0 1 * ?")
        );
        final TradingConfig tradingConfig1 = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final SimulationResult simulationResult1 = SimulationResult.builder()
                .tradingConfig(tradingConfig1)
                .interval(interval)
                .initialBalance(balanceConfig.getInitialBalance())
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

        final TradingConfig tradingConfig2 = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.GOLDEN_CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));
        final SimulatedPosition simulatedPosition1 = new SimulatedPosition(ticker, BigDecimal.valueOf(100000), 10);
        final SimulationResult simulationResult2 = SimulationResult.builder()
                .tradingConfig(tradingConfig2)
                .interval(interval)
                .initialBalance(balanceConfig.getInitialBalance())
                .totalInvestment(BigDecimal.valueOf(10000))
                .weightedAverageInvestment(BigDecimal.valueOf(10000))
                .finalTotalBalance(BigDecimal.valueOf(20000))
                .finalBalance(BigDecimal.valueOf(10000))
                .absoluteProfit(BigDecimal.valueOf(1000))
                .relativeProfit(1.0)
                .relativeYearProfit(12.0)
                .positions(List.of(simulatedPosition1))
                .build();

        final TradingConfig tradingConfig3 = new TradingConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.GOLDEN_CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));
        final SimulatedPosition simulatedPosition2 = new SimulatedPosition(ticker, BigDecimal.valueOf(100), 10);
        final SimulatedPosition simulatedPosition3 = new SimulatedPosition(ticker, BigDecimal.valueOf(1000), 1);
        final SimulationResult simulationResult3 = SimulationResult.builder()
                .tradingConfig(tradingConfig3)
                .interval(interval)
                .initialBalance(balanceConfig.getInitialBalance())
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
                        Mockito.eq(balanceConfig),
                        Mockito.anyList(),
                        Mockito.any(Interval.class),
                        Mockito.eq(true)
                )
        ).thenReturn(simulationResults);

        final String expectedResponse = ResourceUtils.getTestDataAsString("SimulateResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/simulate").content(request).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));
    }

    // endregion

    @Test
    void enableScheduling_enablesScheduling() throws Exception {
        scheduledBotProperties.setEnabled(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/enable").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assertions.assertTrue(scheduledBotProperties.isEnabled());
    }

    @Test
    void disableScheduling_disablesScheduling() throws Exception {
        scheduledBotProperties.setEnabled(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/disable").contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Assertions.assertFalse(scheduledBotProperties.isEnabled());
    }

    // region setTickers tests

    @Test
    void setTickers_setsTickers() throws Exception {
        scheduledBotProperties.setTickers(Set.of());

        final String tickers = ResourceUtils.getTestDataAsString("SetTickersRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/tickers").content(tickers).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        final Set<String> tickersList = scheduledBotProperties.getTickers();
        Assertions.assertEquals(3, tickersList.size());
        Assertions.assertTrue(tickersList.contains("ticker1"));
        Assertions.assertTrue(tickersList.contains("ticker2"));
        Assertions.assertTrue(tickersList.contains("ticker3"));
    }

    @Test
    void setTickers_returnsBadRequest_whenNoTickers() throws Exception {
        scheduledBotProperties.setTickers(Set.of());

        final String tickers = "{}";

        assertBadRequestError("/trader/bot/tickers", tickers, "tickers are mandatory");
    }

    @Test
    void setTickers_returnsBadRequest_whenTickersAreEmpty() throws Exception {
        scheduledBotProperties.setTickers(Set.of());

        final String tickers = "{\"tickers\": []}";

        assertBadRequestError("/trader/bot/tickers", tickers, "tickers are mandatory");
    }

    // endregion

}