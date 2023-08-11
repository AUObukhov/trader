package ru.obukhov.trader.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.market.model.transform.CandleMapper;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.trading.model.BackTestPosition;
import ru.obukhov.trader.trading.model.BackTestResult;
import ru.obukhov.trader.trading.model.Balances;
import ru.obukhov.trader.trading.model.Profits;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.obukhov.trader.web.model.exchange.BackTestRequest;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class BotControllerIntegrationTest extends ControllerIntegrationTest {

    private static final CandleMapper CANDLE_MAPPER = Mappers.getMapper(CandleMapper.class);

    @Autowired
    private SchedulingProperties schedulingProperties;

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
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.0,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        postAndExpectBadRequestError("/trader/bot/back-test", request, "from is mandatory");
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
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.0,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        postAndExpectBadRequestError("/trader/bot/back-test", request, "balanceConfig is mandatory");
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

        postAndExpectBadRequestError("/trader/bot/back-test", request, "botConfigs is mandatory");
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

        postAndExpectBadRequestError("/trader/bot/back-test", request, "botConfigs is mandatory");
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
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                null,
                0.0,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        postAndExpectBadRequestError("/trader/bot/back-test", request, "candleInterval is mandatory");
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
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                null,
                StrategyType.CONSERVATIVE,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        postAndExpectBadRequestError("/trader/bot/back-test", request, "commission is mandatory");
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
                TestData.ACCOUNT_ID1,
                TestShare1.FIGI,
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.0,
                null,
                Map.of("minimumProfit", 0.01)
        );
        request.setBotConfigs(List.of(botConfig));

        postAndExpectBadRequestError("/trader/bot/back-test", request, "strategyType is mandatory");
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void backTest_returnsBackTestResults_whenRequestIsValid() throws Exception {
        final String accountId = TestData.ACCOUNT_ID1;
        final String figi = TestShare1.FIGI;
        final String currency = TestShare1.CURRENCY;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 2, 1);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        // building request

        final BackTestRequest request = new BackTestRequest();

        request.setFrom(from);
        request.setTo(to);
        request.setBalanceConfig(TestData.createBalanceConfig(1000.0, 100.0, "0 0 0 1 * ?"));
        request.setSaveToFiles(false);

        final Map<String, Object> strategyParams1 = Map.of("minimumProfit", 0.01);

        final BotConfig botConfig1 = new BotConfig(
                accountId,
                figi,
                candleInterval,
                0.001,
                StrategyType.CONSERVATIVE,
                strategyParams1
        );

        final Map<String, Object> strategyParams2 = Map.of(
                "minimumProfit", 0.01,
                "movingAverageType", MovingAverageType.SIMPLE,
                "order", 1,
                "smallWindow", 100,
                "bigWindow", 200,
                "indexCoefficient", 0.3,
                "greedy", false
        );
        final BotConfig botConfig2 = new BotConfig(
                accountId,
                figi,
                candleInterval,
                0.002,
                StrategyType.CROSS,
                strategyParams2
        );
        request.setBotConfigs(List.of(botConfig1, botConfig2));

        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/trader/bot/back-test")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);

        // mocking

        Mocker.mockShare(instrumentsService, TestShare1.SHARE);
        Mocker.mockInstrument(instrumentsService, TestInstrument1.INSTRUMENT);

        final String candlesString = ResourceUtils.getTestDataAsString("candles.json");
        final Candle[] candles = TestUtils.OBJECT_MAPPER.readValue(candlesString, Candle[].class);
        final List<HistoricCandle> historicCandles = Arrays.stream(candles)
                .map(candle -> CANDLE_MAPPER.map(candle, true))
                .toList();

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandles)
                .mock();

        final OffsetTime startTime = DateTimeTestData.createTime(12, 0, 0);
        final OffsetTime endTime = DateTimeTestData.createTime(20, 0, 0);
        Mocker.mockTradingSchedule(instrumentsService, TestShare1.EXCHANGE, interval, startTime, endTime);

        // building expected response

        final BigDecimal initialBalance = DecimalUtils.setDefaultScale(1000);

        final Balances balances1 = new Balances(
                initialBalance,
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(1000),
                DecimalUtils.setDefaultScale(54.49544),
                DecimalUtils.setDefaultScale(1061.21544)
        );
        final Operation operation = Operation.newBuilder()
                .setFigi(figi)
                .setDate(TimestampUtils.newTimestamp(from))
                .setOperationType(OperationType.OPERATION_TYPE_BUY)
                .setPrice(TestData.createMoneyValue(10000, currency))
                .setQuantity(1L)
                .build();
        final Candle candle = new CandleBuilder()
                .setOpenPrice(10000)
                .setClosePrice(20000)
                .setHighestPrice(30000)
                .setLowestPrice(5000)
                .setTime(TimestampUtils.newTimestamp(from))
                .build();
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
        final BackTestPosition backTestPosition2 = new BackTestPosition(
                figi,
                DecimalUtils.setDefaultScale(100000),
                DecimalUtils.setDefaultScale(10)
        );
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

        // action & assertion

        performAndExpectResponse(requestBuilder, backTestResults);
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