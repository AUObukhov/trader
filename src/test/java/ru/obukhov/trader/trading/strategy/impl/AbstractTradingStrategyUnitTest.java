package ru.obukhov.trader.trading.strategy.impl;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class AbstractTradingStrategyUnitTest {

    private static final TradingProperties TRADING_PROPERTIES = new TradingProperties();

    @BeforeAll
    static void setUp() {
        TRADING_PROPERTIES.setCommission(0.003);
    }

    @Test
    void constructor_throwsIllegalArgumentException_whenNameIsTooLong() {
        final String name = "abcdefghijklmnopqrstuvwxyz123456";

        AssertUtils.assertThrowsWithMessage(
                () -> new TestStrategy(name, 0.1f, TRADING_PROPERTIES),
                IllegalArgumentException.class,
                "name must be shorter than " + AbstractTradingStrategy.NAME_LENGTH_LIMIT
        );
    }

    // region getBuyOrWaitDecision tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetBuyOrWaitDecision() {
        return Stream.of(
                Arguments.of(1000.0, 1000.0, 1, DecisionAction.WAIT, null),
                Arguments.of(10030.0, 1000.0, 10, DecisionAction.BUY, 1),
                Arguments.of(20059.0, 1000.0, 10, DecisionAction.BUY, 1),
                Arguments.of(20060.0, 1000.0, 10, DecisionAction.BUY, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetBuyOrWaitDecision")
    void getBuyOrWaitDecision(
            double balance,
            double currentPrice,
            int lotSize,
            DecisionAction expectedAction,
            @Nullable Integer expectedLots
    ) {
        final AbstractTradingStrategy strategy = new TestStrategy(0.1f, TRADING_PROPERTIES);
        final DecisionData data = TestDataHelper.createDecisionData(balance, currentPrice, lotSize);
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
        final AbstractTradingStrategy strategy = new TestStrategy(0.1f, TRADING_PROPERTIES);
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
            double averagePositionPrice,
            int positionLotsCount,
            double currentPrice,
            DecisionAction expectedAction,
            @Nullable Integer expectedLots
    ) {
        final AbstractTradingStrategy strategy = new TestStrategy(0.1f, TRADING_PROPERTIES);
        final DecisionData data =
                TestDataHelper.createDecisionData(averagePositionPrice, positionLotsCount, currentPrice);
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

        public TestStrategy(final float minimumProfit, final TradingProperties tradingProperties) {
            super(StringUtils.EMPTY, minimumProfit, tradingProperties);
        }

        public TestStrategy(final String name, final float minimumProfit, final TradingProperties tradingProperties) {
            super(name, minimumProfit, tradingProperties);
        }

        @Override
        public Decision decide(final DecisionData data, final StrategyCache strategyCache) {
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