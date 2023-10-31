package ru.obukhov.trader.trading.strategy.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.trading.model.CrossStrategyParams;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CrossStrategyUnitTest {

    @Mock
    private ExtMarketDataService extMarketDataService;
    @Mock
    private MovingAverager averager;

    // region decide tests

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                TestShares.SBER.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Decision decision = strategy.decide(data, 1, strategy.initCache(botConfig, interval));

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getQuantity());
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsNone() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5);
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.NONE)) {
            final BotConfig botConfig = new BotConfig(
                    TestAccounts.TINKOFF.account().id(),
                    TestShares.SBER.share().figi(),
                    CandleInterval.CANDLE_INTERVAL_1_MIN,
                    DecimalUtils.ZERO,
                    StrategyType.CROSS,
                    Map.of()
            );
            final Interval interval = Interval.of(
                    DateTimeTestData.newDateTime(2023, 9, 10),
                    DateTimeTestData.newDateTime(2023, 9, 11)
            );

            final Decision decision = strategy.decide(data, 1, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    void decide_returnsBuy_whenCrossoverIsBelow_andThereAreAvailableLots() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);
        final int availableLots = 9;

        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                TestShares.SBER.share().figi(),
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final Decision decision = strategy.decide(data, availableLots, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            AssertUtils.assertEquals(availableLots, decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsBelow_andThereAreNoAvailableLots() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final BotConfig botConfig = new BotConfig(
                    TestAccounts.TINKOFF.account().id(),
                    TestShares.SBER.share().figi(),
                    CandleInterval.CANDLE_INTERVAL_1_MIN,
                    DecimalUtils.ZERO,
                    StrategyType.CROSS,
                    Map.of()
            );
            final Interval interval = Interval.of(
                    DateTimeTestData.newDateTime(2023, 9, 10),
                    DateTimeTestData.newDateTime(2023, 9, 11)
            );

            final Decision decision = strategy.decide(data, 0, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    void decide_returnsSell_whenCrossoverIsAbove_andSellProfitIsGreaterThanMinimum() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);
        final int quantity = 10;
        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(100)
                .setQuantity(quantity)
                .build();
        data.setPosition(position);

        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(120));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, 1, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.SELL, decision.getAction());
            AssertUtils.assertEquals(quantity, decision.getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsAbove_andMinimumProfitIsNegative() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                -0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );

        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(100)
                .build();
        data.setPosition(position);

        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, 1, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    void decide_returnsBuy_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andGreedy() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                true,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        data.setPosition(position);
        final int availableLots = 4;

        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, availableLots, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            AssertUtils.assertEquals(availableLots, decision.getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andNotGreedy() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(1000.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        data.setPosition(position);

        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, 1, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreNoAvailableLots() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, extMarketDataService, averager);

        final DecisionData data = TestData.newDecisionData(200.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        data.setPosition(position);

        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CROSS,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(BigDecimal.ZERO);
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        try (@SuppressWarnings("unused") final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, 0, strategy.initCache(botConfig, interval));

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        final String figi = TestShares.SBER.share().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;
        final BotConfig botConfig = new BotConfig(
                TestAccounts.TINKOFF.account().id(),
                figi,
                candleInterval,
                DecimalUtils.ZERO,
                StrategyType.CONSERVATIVE,
                Map.of()
        );
        final Interval interval = Interval.of(
                DateTimeTestData.newDateTime(2023, 9, 10),
                DateTimeTestData.newDateTime(2023, 9, 11)
        );

        final Candle candle = new Candle().setClose(DecimalUtils.setDefaultScale(100));
        final List<Candle> candles = List.of(candle);
        Mockito.when(extMarketDataService.getCandles(figi, interval, candleInterval)).thenReturn(candles);

        final ConservativeStrategy strategy = new ConservativeStrategy(StringUtils.EMPTY, extMarketDataService);

        Assertions.assertNotNull(strategy.initCache(botConfig, interval));
    }

    private static MockedStatic<TrendUtils> mock_TrendUtils_getCrossoverIfLast(Crossover crossover) {
        final MockedStatic<TrendUtils> trendUtilsStaticMock = Mockito.mockStatic(TrendUtils.class, Mockito.CALLS_REAL_METHODS);

        trendUtilsStaticMock.when(() -> TrendUtils.getCrossoverIfLast(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt()))
                .thenReturn(crossover);

        return trendUtilsStaticMock;
    }

}