package ru.obukhov.trader.trading.strategy.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.model.TradingStrategyParams;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @ParameterizedTest
    @ValueSource(longs = {-1, 0})
    void getBuyOrWaitDecision_throwsIllegalArgumentException_whenLotsQuantityIsNotPositive(final long lotsQuantity) {
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData data = TestData.newDecisionData2(1, 10L);

        final Executable executable = () -> strategy.getBuyOrWaitDecision(data, lotsQuantity);
        final String expectedMessage = "lotsQuantity must be above 0. Got " + lotsQuantity;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetBuyOrWaitDecision() {
        return Stream.of(
                Arguments.of(3L, 2L, DecisionAction.WAIT, null),
                Arguments.of(2L, 2L, DecisionAction.BUY, 2L),
                Arguments.of(2L, 3L, DecisionAction.BUY, 2L)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetBuyOrWaitDecision")
    void getBuyOrWaitDecision(final long lotsQuantity, final long availableLots, final DecisionAction expectedAction, final Long expectedQuantity) {
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData data = TestData.newDecisionData2(1, availableLots);

        final Decision decision = strategy.getBuyOrWaitDecision(data, lotsQuantity);

        Assertions.assertEquals(expectedAction, decision.getAction());
        Assertions.assertEquals(expectedQuantity, decision.getQuantity());
    }

    // endregion

    // region getSellOrWaitDecision tests

    @Test
    void getSellOrWaitDecision_returnsWait_whenPositionIsNull() {
        final float minimumProfit = 0.1f;
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData decisionData = new DecisionData();

        final BigDecimal currentPrice = DecimalUtils.ONE;
        final BigDecimal commission = DecimalUtils.ZERO;
        final Decision decision = strategy.getSellOrWaitDecision(decisionData, currentPrice, commission, minimumProfit);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getQuantity());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetSellOrWaitDecision() {
        return Stream.of(
                Arguments.of(0.1f, 1000, 10L, 1100.0, DecisionAction.WAIT),
                Arguments.of(0.1f, 1000, 30L, 900.0, DecisionAction.WAIT),
                Arguments.of(0.1f, 100, 20L, 1000.0, DecisionAction.SELL),
                Arguments.of(-1.0f, 100, 20L, 1000.0, DecisionAction.WAIT)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSellOrWaitDecision")
    void getSellOrWaitDecision(
            final float minimumProfit,
            final int averagePositionPrice,
            final Long quantity,
            final double currentPrice,
            final DecisionAction expectedAction
    ) {
        final AbstractTradingStrategy strategy = new TestStrategy("testStrategy", null);
        final DecisionData data = TestData.newDecisionData3(averagePositionPrice, quantity);

        final Decision decision = strategy.getSellOrWaitDecision(
                data,
                DecimalUtils.setDefaultScale(currentPrice),
                DecimalUtils.ZERO,
                minimumProfit
        );

        Assertions.assertEquals(expectedAction, decision.getAction());
        if (expectedAction == DecisionAction.WAIT) {
            Assertions.assertNull(decision.getQuantity());
        } else {
            Assertions.assertEquals(quantity, decision.getQuantity());
        }
    }

    // endregion

    // region existsOperationInProgress tests

    @Test
    void existsOperationInProgress_returnsTrue_whenOperationInUnspecifiedStateExists() {
        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_UNSPECIFIED);
        final Operation operation3 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData decisionData = new DecisionData();
        decisionData.setLastOperations(List.of(operation1, operation2, operation3));

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(decisionData));

        Assertions.assertTrue(TestStrategy.existsOperationInProgress(decisionsData));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenOperationInProgressDoesNotExists() {
        final Operation operation1 = TestData.newOperation(OperationState.OPERATION_STATE_EXECUTED);
        final Operation operation2 = TestData.newOperation(OperationState.OPERATION_STATE_CANCELED);

        final DecisionData decisionData = new DecisionData();
        decisionData.setLastOperations(List.of(operation1, operation2));

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(decisionData));

        Assertions.assertFalse(TestStrategy.existsOperationInProgress(decisionsData));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenNoOperations() {
        final DecisionData decisionData = new DecisionData();
        decisionData.setLastOperations(Collections.emptyList());

        final DecisionsData decisionsData = new DecisionsData();
        decisionsData.setDecisionDatas(List.of(decisionData));

        Assertions.assertFalse(TestStrategy.existsOperationInProgress(decisionsData));
    }

    // endregion

    private static class TestStrategy extends AbstractTradingStrategy {

        public TestStrategy(final String name, final TradingStrategyParams params) {
            super(name, params);
        }

        @Override
        public Map<String, Decision> decide(
                final DecisionsData data,
                final BotConfig botConfig,
                final Interval interval
        ) {
            throw new NotImplementedException();
        }

        public static boolean existsOperationInProgress(final DecisionsData data) {
            return AbstractTradingStrategy.existsOperationStateIsUnspecified(data);
        }

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