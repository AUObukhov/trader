package ru.obukhov.trader.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.ScheduledBotsProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.obukhov.trader.web.model.exchange.BackTestRequest;
import ru.obukhov.trader.web.model.exchange.BackTestResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class BotControllerIntegrationTest extends ControllerIntegrationTest {

    @Autowired
    private SchedulingProperties schedulingProperties;
    @Autowired
    private ScheduledBotsProperties scheduledBotsProperties;

    // region backTest tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenFromIsNull() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(null);
        request.setTo(null);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(false);
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                "ticker",
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.0,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        performAndExpectBadRequestError("/trader/bot/back-test", request, "from is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenBalanceConfigIsNull() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(DateTimeTestData.createDateTime(2021, 1, 1, 10));
        request.setTo(DateTimeTestData.createDateTime(2021, 2, 1));
        request.setBalanceConfig(null);
        request.setSaveToFiles(true);
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                "ticker",
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.0,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        performAndExpectBadRequestError("/trader/bot/back-test", request, "balanceConfig is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenBotConfigsIsNull() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(DateTimeTestData.createDateTime(2021, 1, 1, 10));
        request.setTo(null);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(true);
        request.setBotConfigs(null);

        performAndExpectBadRequestError("/trader/bot/back-test", request, "botConfigs is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenBotsConfigsIsEmpty() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(DateTimeTestData.createDateTime(2021, 1, 1, 10));
        request.setTo(null);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(true);
        request.setBotConfigs(Collections.emptyList());

        performAndExpectBadRequestError("/trader/bot/back-test", request, "botConfigs is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenCandleIntervalIsNull() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(DateTimeTestData.createDateTime(2021, 1, 1, 10));
        request.setTo(null);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(true);
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                "ticker",
                null,
                0.0,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        performAndExpectBadRequestError("/trader/bot/back-test", request, "candleInterval is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenCommissionIsNull() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(DateTimeTestData.createDateTime(2021, 1, 1, 10));
        request.setTo(null);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(true);
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                "ticker",
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        performAndExpectBadRequestError("/trader/bot/back-test", request, "commission is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBadRequest_whenStrategyTypeIsNull() throws Exception {
        final BackTestRequest request = new BackTestRequest();
        request.setFrom(DateTimeTestData.createDateTime(2021, 1, 1, 10));
        request.setTo(null);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(true);
        final BotConfig botConfig = new BotConfig(
                "2000124699",
                "ticker",
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.0,
                null,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        performAndExpectBadRequestError("/trader/bot/back-test", request, "strategyType is mandatory");
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBackTestResults_whenRequestIsValid() throws Exception {
        final String ticker = "ticker";
        final String figi = "figi";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 2, 1);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        // building request

        final BackTestRequest request = new BackTestRequest();

        request.setFrom(from);
        request.setTo(to);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(false);

        final Map<String, Object> strategyParams1 = Map.of("minimumProfit", 0.01);

        final BotConfig botConfig1 = BotConfig.builder()
                .ticker(ticker)
                .candleInterval(candleInterval)
                .commission(0.001)
                .strategyType(StrategyType.CONSERVATIVE)
                .strategyParams(strategyParams1)
                .build();

        final Map<String, Object> strategyParams2 = Map.of(
                "minimumProfit", 0.01,
                "movingAverageType", MovingAverageType.SIMPLE,
                "order", 1,
                "smallWindow", 100,
                "bigWindow", 200,
                "indexCoefficient", 0.3,
                "greedy", false
        );
        final BotConfig botConfig2 = BotConfig.builder()
                .ticker(ticker)
                .candleInterval(candleInterval)
                .commission(0.002)
                .strategyType(StrategyType.CROSS)
                .strategyParams(strategyParams2)
                .build();

        request.setBotConfigs(List.of(botConfig1, botConfig2));

        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/bot/back-test")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);

        // mocking

        final MarketInstrument instrument = new MarketInstrument(figi, null, null, null, 1, null, Currency.USD, null, null);
        mockInstrumentByTicker(ticker, instrument);

        final String candlesString = ResourceUtils.getTestDataAsString("candles.json");
        final Candle[] array = TestUtils.OBJECT_MAPPER.readValue(candlesString, Candle[].class);
        final List<Candle> candles = Arrays.asList(array);
        mockAllCandles(figi, candles);

        // building expected response

        final Interval interval = Interval.of(from, to);

        final BigDecimal initialBalance = DecimalUtils.setDefaultScale(1000);

        final Balances balances1 = new Balances(
                initialBalance,
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(54.49544),
                DecimalUtils.setDefaultScale(1061.21544)
        );
        final BackTestOperation operation = new BackTestOperation(
                ticker,
                from,
                OperationType.BUY,
                DecimalUtils.setDefaultScale(10000),
                1,
                DecimalUtils.setDefaultScale(300)
        );
        final Candle candle = TestData.createCandle(10000, 20000, 30000, 5000, from, CandleInterval.CANDLE_INTERVAL_1_MIN);
        final BackTestResult backTestResult1 = new BackTestResult(
                botConfig1,
                interval,
                balances1,
                new Profits(DecimalUtils.setDefaultScale(61.215440), 0.06121544, 1.033135028),
                Collections.emptyList(),
                List.of(operation),
                List.of(candle),
                null
        );

        final Balances balances2 = new Balances(
                initialBalance,
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(1000)
        );
        final BackTestPosition backTestPosition2 = new BackTestPosition(ticker, DecimalUtils.setDefaultScale(100000), 10);
        final BackTestResult backTestResult2 = new BackTestResult(
                botConfig2,
                interval,
                balances2,
                new Profits(DecimalUtils.setDefaultScale(0), 0.0, 0.0),
                List.of(backTestPosition2),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        final List<BackTestResult> backTestResults = List.of(backTestResult1, backTestResult2);

        final BackTestResponse response = new BackTestResponse(backTestResults);

        // action & assertion

        performAndExpectResponse(requestBuilder, response);
    }

    // endregion

    @Test
    void enableScheduling_returnsOk_andEnablesScheduling() throws Exception {
        schedulingProperties.setEnabled(false);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/enable-scheduling"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));

        Assertions.assertTrue(schedulingProperties.isEnabled());
    }

    @Test
    void disableScheduling_returnsOk_andDisablesScheduling() throws Exception {
        schedulingProperties.setEnabled(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/bot/disable-scheduling"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(StringUtils.EMPTY));

        Assertions.assertFalse(schedulingProperties.isEnabled());
    }

}