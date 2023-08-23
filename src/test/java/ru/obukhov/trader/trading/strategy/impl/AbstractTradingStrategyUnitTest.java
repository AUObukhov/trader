package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.TradingStrategyParams;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class AbstractTradingStrategyUnitTest {

    // region getName tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetName_returnsProperName() {
        return Stream.of(
                Arguments.of("testStrategy", null, "testStrategy"),
                Arguments.of("testStrategy", new TestTradingStrategyParams("fieldValue"), "testStrategy [field=fieldValue]")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetName_returnsProperName")
    void getName_returnsProperName(final String name, final TradingStrategyParams params, final String expectedValue) {
        final TradingStrategy strategy = new TestStrategy(name, params);

        Assertions.assertEquals(expectedValue, strategy.getName());
    }

    // endregion

    // region getBuyOrWaitDecision tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetBuyOrWaitDecision() {
        return Stream.of(
                Arguments.of(1000.0, 1000.0, 1, DecisionAction.WAIT, null),
                Arguments.of(10030.0, 1000.0, 10, DecisionAction.BUY, 1L),
                Arguments.of(20019.0, 1000.0, 10, DecisionAction.BUY, 1L),
                Arguments.of(20060.0, 1000.0, 10, DecisionAction.BUY, 2L)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetBuyOrWaitDecision")
    void getBuyOrWaitDecision(
            final double balance,
            final double currentPrice,
            final int lotSize,
            final DecisionAction expectedAction,
            @Nullable final Long expectedLots
    ) {
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData data = TestData.createDecisionData(balance, currentPrice, lotSize, 0.003);
        final StrategyCache strategyCache = new TestStrategyCache();

        final Decision decision = strategy.getBuyOrWaitDecision(data, strategyCache);

        Assertions.assertEquals(expectedAction, decision.getAction());
        Assertions.assertEquals(expectedLots, decision.getQuantity());
        Assertions.assertSame(strategyCache, decision.getStrategyCache());
    }

    // endregion

    // region getSellOrWaitDecision tests

    @Test
    void getSellOrWaitDecision_returnsWait_whenPositionIsNull() {
        final float minimumProfit = 0.1f;
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData decisionData = new DecisionData();
        final StrategyCache strategyCache = new TestStrategyCache();

        final Decision decision = strategy.getSellOrWaitDecision(decisionData, minimumProfit, strategyCache);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getQuantity());
        Assertions.assertSame(strategyCache, decision.getStrategyCache());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetSellOrWaitDecision() {
        return Stream.of(
                Arguments.of(0.1f, 1000.0, 10, 1100.0, DecisionAction.WAIT),
                Arguments.of(0.1f, 1000.0, 30, 900.0, DecisionAction.WAIT),
                Arguments.of(0.1f, 100.0, 20, 1000.0, DecisionAction.SELL),
                Arguments.of(-1.0f, 100.0, 20, 1000.0, DecisionAction.WAIT)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSellOrWaitDecision")
    void getSellOrWaitDecision(
            final float minimumProfit,
            final double averagePositionPrice,
            final int quantity,
            final double currentPrice,
            final DecisionAction expectedAction
    ) {
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData data = TestData.createDecisionData(averagePositionPrice, quantity, currentPrice);
        final StrategyCache strategyCache = new TestStrategyCache();

        final Decision decision = strategy.getSellOrWaitDecision(data, minimumProfit, strategyCache);

        Assertions.assertEquals(expectedAction, decision.getAction());
        if (expectedAction == DecisionAction.WAIT) {
            Assertions.assertNull(decision.getQuantity());
        } else {
            AssertUtils.assertEquals(quantity, decision.getQuantity());
        }
        Assertions.assertSame(strategyCache, decision.getStrategyCache());
    }

    // endregion

    // region existsOperationInProgress tests

    @Test
    void existsOperationInProgress_returnsTrue_whenOperationInUnspecifiedStateExists() {
        final Operation operation1 = TestData.createOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.createOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.createOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        Assertions.assertTrue(TestStrategy.existsOperationInProgress(data));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenOperationInProgressDoesNotExists() {
        final Operation operation1 = TestData.createOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.createOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2));

        Assertions.assertFalse(TestStrategy.existsOperationInProgress(data));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenNoOperations() {
        final DecisionData data = new DecisionData();
        data.setLastOperations(Collections.emptyList());

        Assertions.assertFalse(TestStrategy.existsOperationInProgress(data));
    }

    // endregion

    private static class TestStrategy extends AbstractTradingStrategy {

        public TestStrategy(final String name, final TradingStrategyParams params) {
            super(name, params);
        }

        @Override
        public Decision decide(@NotNull final DecisionData data, @NotNull final StrategyCache strategyCache) {
            throw new NotImplementedException();
        }

        public static boolean existsOperationInProgress(final DecisionData data) {
            return AbstractTradingStrategy.existsOperationStateIsUnspecified(data);
        }

        @NotNull
        @Override
        public StrategyCache initCache() {
            throw new NotImplementedException();
        }

    }

    private static class TestStrategyCache implements StrategyCache {
    }

    @AllArgsConstructor
    private static class TestTradingStrategyParams implements TradingStrategyParams {

        private final String field;

        @Override
        public String toString() {
            return "[field=" + field + ']';
        }
    }

}