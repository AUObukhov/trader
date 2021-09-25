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
import ru.obukhov.trader.trading.backtest.interfaces.BackTester;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class BotControllerWebTest extends ControllerWebTest {

    @MockBean
    private BackTester backTester;

    @Autowired
    private ScheduledBotProperties scheduledBotProperties;

    // region test tests

    @Test
    void test_returnsBadRequest_whenFromIsNull() throws Exception {
        test_returnsBadRequest_withError("BackTestRequest/BackTestRequest_fromIsNull.json", "from is mandatory");
    }

    @Test
    void test_returnsBadRequest_whenBalanceConfigIsNull() throws Exception {
        test_returnsBadRequest_withError(
                "BackTestRequest/BackTestRequest_balanceConfigsIsNull.json",
                "balanceConfig is mandatory"
        );
    }

    @Test
    void test_returnsBadRequest_whenBotConfigsIsNull() throws Exception {
        test_returnsBadRequest_withError(
                "BackTestRequest/BackTestRequest_botConfigsIsNull.json",
                "botConfigs is mandatory"
        );
    }

    @Test
    void test_returnsBadRequest_whenBotsConfigsIsEmpty() throws Exception {
        test_returnsBadRequest_withError(
                "BackTestRequest/BackTestRequest_botConfigsIsEmpty.json",
                "botConfigs is mandatory"
        );
    }

    @Test
    void test_returnsBadRequest_whenCandleResolutionIsNull() throws Exception {
        test_returnsBadRequest_withError(
                "BackTestRequest/BackTestRequest_candleResolutionIsNull.json",
                "candleResolution is mandatory"
        );
    }

    @Test
    void test_returnsBadRequest_whenStrategyTypeIsNull() throws Exception {
        test_returnsBadRequest_withError(
                "BackTestRequest/BackTestRequest_strategyTypeIsNull.json",
                "strategyType is mandatory"
        );
    }

    private void test_returnsBadRequest_withError(String backTestRequestResource, String expectedError) throws Exception {
        final String requestString = ResourceUtils.getTestDataAsString(backTestRequestResource);

        assertBadRequestError("/trader/bot/back-test", requestString, expectedError);
    }

    @Test
    void test_returnsBackTestResults_whenRequestIsValid() throws Exception {
        final String ticker = "ticker";

        final String request = ResourceUtils.getTestDataAsString("BackTestRequest/BackTestRequest_valid.json");

        final List<BackTestResult> backTestResults = new ArrayList<>();
        final OffsetDateTime from = DateTimeTestData.createDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2021, 2, 1);
        final Interval interval = Interval.of(from, to);

        final BackTestOperation operation = new BackTestOperation(
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
        final BotConfig botConfig1 = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("minimumProfit", 0.01));
        final BackTestResult backTestResult1 = BackTestResult.builder()
                .botConfig(botConfig1)
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

        final BotConfig botConfig2 = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));
        final BackTestPosition backTestPosition1 = new BackTestPosition(ticker, BigDecimal.valueOf(100000), 10);
        final BackTestResult backTestResult2 = BackTestResult.builder()
                .botConfig(botConfig2)
                .interval(interval)
                .initialBalance(balanceConfig.getInitialBalance())
                .totalInvestment(BigDecimal.valueOf(10000))
                .weightedAverageInvestment(BigDecimal.valueOf(10000))
                .finalTotalBalance(BigDecimal.valueOf(20000))
                .finalBalance(BigDecimal.valueOf(10000))
                .absoluteProfit(BigDecimal.valueOf(1000))
                .relativeProfit(1.0)
                .relativeYearProfit(12.0)
                .positions(List.of(backTestPosition1))
                .build();

        final BotConfig botConfig3 = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));
        final BackTestPosition backTestPosition2 = new BackTestPosition(ticker, BigDecimal.valueOf(100), 10);
        final BackTestPosition backTestPosition3 = new BackTestPosition(ticker, BigDecimal.valueOf(1000), 1);
        final BackTestResult backTestResult3 = BackTestResult.builder()
                .botConfig(botConfig3)
                .interval(interval)
                .initialBalance(balanceConfig.getInitialBalance())
                .totalInvestment(BigDecimal.valueOf(2000))
                .weightedAverageInvestment(BigDecimal.valueOf(2000))
                .finalTotalBalance(BigDecimal.valueOf(4000))
                .finalBalance(BigDecimal.valueOf(3000))
                .absoluteProfit(BigDecimal.valueOf(1000))
                .relativeProfit(0.33)
                .relativeYearProfit(4.0)
                .positions(List.of(backTestPosition2, backTestPosition3))
                .error("error")
                .build();

        backTestResults.add(backTestResult1);
        backTestResults.add(backTestResult2);
        backTestResults.add(backTestResult3);

        Mockito.when(backTester.test(Mockito.anyList(), Mockito.eq(balanceConfig), Mockito.any(Interval.class), Mockito.eq(true)))
                .thenReturn(backTestResults);

        final String expectedResponse = ResourceUtils.getTestDataAsString("BackTestResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/back-test").content(request).contentType(MediaType.APPLICATION_JSON))
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

}