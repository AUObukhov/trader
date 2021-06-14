package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.model.LinearGoldenCrossStrategyParams;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.List;

class LinearGoldenCrossStrategyUnitTest {

    private static final TradingProperties TRADING_PROPERTIES = new TradingProperties();

    @BeforeAll
    static void setUp() {
        TRADING_PROPERTIES.setCommission(0.003);
    }

    @Test
    void getName_returnsProperName() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        Assertions.assertEquals("Plain Linear Golden Cross (0.1, 3-6-0.6)", strategy.getName());
    }

    // region decide tests

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final Operation operation1 = new Operation().status(OperationStatus.DONE);
        final Operation operation2 = new Operation().status(OperationStatus.PROGRESS);
        final Operation operation3 = new Operation().status(OperationStatus.DECLINE);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        final Decision decision = strategy.decide(data, strategy.initCache());

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsNone() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(1000.0, 100.0, 1);

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.NONE)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsBelow_andThereAreAvailableLots() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(1000.0, 100.0, 1);

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            Assertions.assertEquals(9, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsBelow_andThereAreNoAvailableLots() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(1000.0, 1000.0, 1);

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsSell_whenCrossoverIsAbove_andSellProfitIsGreaterThanMinimum() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(1000.0, 200.0, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(100, 10));

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.SELL, decision.getAction());
            Assertions.assertEquals(10, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsMinusOne_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andGreedy() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, true, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(1000.0, 200.0, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(199, 10));

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            Assertions.assertEquals(4, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andNotGreedy() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(1000.0, 200.0, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(199, 10));

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreNoAvailableLots() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        final DecisionData data = TestDataHelper.createDecisionData(200.0, 200.0, 1);
        data.setPosition(TestDataHelper.createPortfolioPosition(199, 10));

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        final LinearGoldenCrossStrategy strategy = new LinearGoldenCrossStrategy(
                0.1f,
                TRADING_PROPERTIES,
                new LinearGoldenCrossStrategyParams(0.6f, false, 3, 6)
        );

        Assertions.assertNotNull(strategy.initCache());
    }

    private static MockedStatic<TrendUtils> mock_TrendUtils_getCrossoverIfLast(Crossover crossover) {
        final MockedStatic<TrendUtils> trendUtilsStaticMock =
                Mockito.mockStatic(TrendUtils.class, Mockito.CALLS_REAL_METHODS);

        trendUtilsStaticMock.when(
                () -> TrendUtils.getCrossoverIfLast(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt())
        ).thenReturn(crossover);

        return trendUtilsStaticMock;
    }

}