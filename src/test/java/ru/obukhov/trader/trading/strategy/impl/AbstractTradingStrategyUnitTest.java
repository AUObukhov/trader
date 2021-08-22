package ru.obukhov.trader.trading.strategy.impl;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.TradingStrategyParams;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class AbstractTradingStrategyUnitTest {

    private static final TradingProperties TRADING_PROPERTIES = TestData.createTradingProperties();

    // region getBuyOrWaitDecision tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetBuyOrWaitDecision() {
        return Stream.of(
                Arguments.of(1000.0, 1000.0, 1, DecisionAction.WAIT, null),
                Arguments.of(10030.0, 1000.0, 10, DecisionAction.BUY, 1),
                Arguments.of(20019.0, 1000.0, 10, DecisionAction.BUY, 1),
                Arguments.of(20060.0, 1000.0, 10, DecisionAction.BUY, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetBuyOrWaitDecision")
    void getBuyOrWaitDecision(
            final double balance,
            final double currentPrice,
            final int lotSize,
            final DecisionAction expectedAction,
            final @Nullable Integer expectedLots
    ) {
        final TradingStrategyParams params = new TradingStrategyParams(0.1f);
        final AbstractTradingStrategy strategy = new TestStrategy(params, TRADING_PROPERTIES);
        final DecisionData data = TestData.createDecisionData(balance, currentPrice, lotSize);
        final StrategyCache strategyCache = new TestStrategyCache();

        final Decision decision = strategy.getBuyOrWaitDecision(data, strategyCache);

        Assertions.assertEquals(expectedAction, decision.getAction());
        Assertions.assertEquals(expectedLots, decision.getLots());
        Assertions.assertSame(strategyCache, decision.getStrategyCache());
    }

    // endregion

    // region getSellOrWaitDecision tests

    @Test
    void getSellOrWaitDecision_returnsWait_whenPositionIsNull() {
        final TradingStrategyParams params = new TradingStrategyParams(0.1f);
        final AbstractTradingStrategy strategy = new TestStrategy(params, TRADING_PROPERTIES);
        final DecisionData decisionData = new DecisionData();
        final StrategyCache strategyCache = new TestStrategyCache();

        final Decision decision = strategy.getSellOrWaitDecision(decisionData, strategyCache);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
        Assertions.assertSame(strategyCache, decision.getStrategyCache());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetSellOrWaitDecision() {
        return Stream.of(
                Arguments.of(1000.0, 10, 1100.0, DecisionAction.WAIT, null),
                Arguments.of(1000.0, 10, 900.0, DecisionAction.WAIT, null),
                Arguments.of(100.0, 10, 1000.0, DecisionAction.SELL, 10)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSellOrWaitDecision")
    void getSellOrWaitDecision(
            final double averagePositionPrice,
            final int positionLotsCount,
            final double currentPrice,
            final DecisionAction expectedAction,
            final @Nullable Integer expectedLots
    ) {
        final TradingStrategyParams params = new TradingStrategyParams(0.1f);
        final AbstractTradingStrategy strategy = new TestStrategy(params, TRADING_PROPERTIES);
        final DecisionData data =
                TestData.createDecisionData(averagePositionPrice, positionLotsCount, currentPrice);
        final StrategyCache strategyCache = new TestStrategyCache();

        final Decision decision = strategy.getSellOrWaitDecision(data, strategyCache);

        Assertions.assertEquals(expectedAction, decision.getAction());
        Assertions.assertEquals(expectedLots, decision.getLots());
        Assertions.assertSame(strategyCache, decision.getStrategyCache());
    }

    // endregion

    // region existsOperationInProgress tests

    @Test
    void existsOperationInProgress_returnsTrue_whenOperationInProgressExists() {
        final Operation operation1 = new Operation().status(OperationStatus.DONE);
        final Operation operation2 = new Operation().status(OperationStatus.PROGRESS);
        final Operation operation3 = new Operation().status(OperationStatus.DECLINE);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        Assertions.assertTrue(TestStrategy.existsOperationInProgress(data));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenOperationInProgressDoesNotExists() {
        final Operation operation1 = new Operation().status(OperationStatus.DONE);
        final Operation operation2 = new Operation().status(OperationStatus.DECLINE);

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

        public TestStrategy(final TradingStrategyParams params, final TradingProperties tradingProperties) {
            super(StringUtils.EMPTY, params, tradingProperties);
        }

        @Override
        public Decision decide(@NotNull final DecisionData data, @NotNull final StrategyCache strategyCache) {
            throw new NotImplementedException();
        }

        public static boolean existsOperationInProgress(final DecisionData data) {
            return AbstractTradingStrategy.existsOperationInProgress(data);
        }

        @NotNull
        @Override
        public StrategyCache initCache() {
            throw new NotImplementedException();
        }

    }

    private static class TestStrategyCache implements StrategyCache {
    }

}