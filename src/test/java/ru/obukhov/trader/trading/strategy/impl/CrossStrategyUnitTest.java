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
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.CrossStrategyParams;
import ru.obukhov.trader.trading.model.Crossover;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;
import ru.tinkoff.piapi.core.models.Position;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class CrossStrategyUnitTest {

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
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final Operation operation1 = TestData.createOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.createOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.createOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        final Decision decision = strategy.decide(data, strategy.initCache());

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
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 100.0, 1, 0.003);

        try (
                final MockedStatic<TrendUtils> trendUtilsStaticMock =
                        mock_TrendUtils_getCrossoverIfLast(Crossover.NONE)
        ) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsBelow_andThereAreAvailableLots() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 100.0, 1, 0.003);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            AssertUtils.assertEquals(9, decision.getQuantity());
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
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 1000.0, 1, 0.003);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.BELOW)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsSell_whenCrossoverIsAbove_andSellProfitIsGreaterThanMinimum() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);
        final int quantity = 10;
        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(100)
                .setQuantity(quantity)
                .build();
        data.setPosition(position);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.SELL, decision.getAction());
            AssertUtils.assertEquals(quantity, decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andMinimumProfitIsNegative() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                -0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(100)
                .build();
        data.setPosition(position);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsBuy_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andGreedy() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                true,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        data.setPosition(position);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
            AssertUtils.assertEquals(4, decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreAvailableLots_andNotGreedy() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(1000.0, 200.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        data.setPosition(position);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    @Test
    @SuppressWarnings("unused")
    void decide_returnsWait_whenCrossoverIsAbove_andSellProfitIsLowerThanMinimum_andThereAreNoAvailableLots() {
        final CrossStrategyParams strategyParams = new CrossStrategyParams(
                0.1f,
                1,
                0.6f,
                false,
                3,
                5
        );
        final CrossStrategy strategy = new CrossStrategy(StringUtils.EMPTY, strategyParams, averager);

        final DecisionData data = TestData.createDecisionData(200.0, 200.0, 1, 0.003);
        final Position position = new PositionBuilder()
                .setAveragePositionPrice(199)
                .build();
        data.setPosition(position);

        try (final MockedStatic<TrendUtils> trendUtilsStaticMock = mock_TrendUtils_getCrossoverIfLast(Crossover.ABOVE)) {
            final Decision decision = strategy.decide(data, strategy.initCache());

            Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
            Assertions.assertNull(decision.getQuantity());
        }
    }

    // endregion

    private static MockedStatic<TrendUtils> mock_TrendUtils_getCrossoverIfLast(Crossover crossover) {
        final MockedStatic<TrendUtils> trendUtilsStaticMock = Mockito.mockStatic(TrendUtils.class, Mockito.CALLS_REAL_METHODS);

        trendUtilsStaticMock.when(() -> TrendUtils.getCrossoverIfLast(Mockito.anyList(), Mockito.anyList(), Mockito.anyInt()))
                .thenReturn(crossover);

        return trendUtilsStaticMock;
    }

}