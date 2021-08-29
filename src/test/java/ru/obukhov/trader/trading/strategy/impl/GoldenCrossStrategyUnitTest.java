package ru.obukhov.trader.trading.strategy.impl;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.common.util.TrendUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.GoldenCrossStrategyParams;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class GoldenCrossStrategyUnitTest {

    private static final TradingProperties TRADING_PROPERTIES = TestData.createTradingProperties();

    @Mock
    private MovingAverager averager;

    // region decide tests

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

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
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5);
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 100.0, 1);

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
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 100.0, 1);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            Assertions.assertEquals(9, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsBelow_andThereAreNoAvailableLots() {
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 1000.0, 1);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsSell_whenCrossoverIsAbove_andSellProfitIsGreaterThanMinimum() {
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1);
        data.setPosition(TestData.createPortfolioPosition(100, 10));

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.SELL, decision.getAction());
            Assertions.assertEquals(10, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsMinusOne_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andGreedy() {
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                true,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1);
        data.setPosition(TestData.createPortfolioPosition(199, 10));

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            Assertions.assertEquals(4, decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andNotGreedy() {
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1);
        data.setPosition(TestData.createPortfolioPosition(199, 10));

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreNoAvailableLots() {
        final GoldenCrossStrategyParams strategyParams = new GoldenCrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final GoldenCrossStrategy strategy = new GoldenCrossStrategy(StringUtils.EMPTY, strategyParams, TRADING_PROPERTIES, averager);

        final DecisionData data = TestData.createDecisionData(200.0, 200.0, 1);
        data.setPosition(TestData.createPortfolioPosition(199, 10));

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getLots());
        }
    }

    // endregion

    private static MockedStatic<TrendUtils> mock_TrendUtils_getCrossoverIfLast(Crossover crossover) {
        final MockedStatic<TrendUtils> trendUtilsStaticMock =
                Mockito.mockStatic(TrendUtils.class, Mockito.CALLS_REAL_METHODS);

        trendUtilsStaticMock.when(() -> TrendUtils.getCrossoverIfLast(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt()))
                .thenReturn(crossover);

        return trendUtilsStaticMock;
    }

}