package ru.obukhov.trader.bot.strategy;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.Collections;
import java.util.List;

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
                () -> new TestStrategy(name, TRADING_PROPERTIES),
                IllegalArgumentException.class,
                "name must be shorter than " + AbstractTradingStrategy.NAME_LENGTH_LIMIT
        );
    }

    // region getProfit tests

    @Test
    void getProfit_returnsZero_whenPositionIsNull() {
        final AbstractTradingStrategy strategy = new TestStrategy(TRADING_PROPERTIES);
        final DecisionData decisionData = new DecisionData();

        final double profit = strategy.getProfit(decisionData);

        AssertUtils.assertEquals(0.0, profit);
    }

    @ParameterizedTest
    @CsvSource({
            "1000.0, 1100.0, 0.09342",
            "1000.0, 900.0, -0.10538",
    })
    void getProfit(double averagePositionPrice, double currentPrice, double expectedProfit) {
        final AbstractTradingStrategy strategy = new TestStrategy(TRADING_PROPERTIES);
        final DecisionData data = TestDataHelper.createDecisionData(averagePositionPrice, currentPrice);

        double profit = strategy.getProfit(data);

        Assertions.assertEquals(expectedProfit, profit);
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

    @ParameterizedTest
    @CsvSource({
            "1000.0, 1, 1000.0, 0",
            "1000.0, 10, 10030.0, 1",
            "1000.0, 10, 20059.0, 1",
            "1000.0, 10, 20060.0, 2"
    })
    void getAvailableLots(double currentPrice, int lotSize, double balance, int expectedAvailableLots) {
        final AbstractTradingStrategy strategy = new TestStrategy(TRADING_PROPERTIES);
        final DecisionData data = TestDataHelper.createDecisionData(balance, currentPrice, lotSize);

        final int availableLots = strategy.getAvailableLots(data);

        Assertions.assertEquals(expectedAvailableLots, availableLots);
    }

    private static class TestStrategy extends AbstractTradingStrategy {

        public TestStrategy(TradingProperties tradingProperties) {
            super(StringUtils.EMPTY, tradingProperties);
        }

        public TestStrategy(String name, TradingProperties tradingProperties) {
            super(name, tradingProperties);
        }

        @Override
        public Decision decide(DecisionData data, StrategyCache strategyCache) {
            throw new NotImplementedException();
        }

        public double getProfit(DecisionData data) {
            return super.getProfit(data);
        }

        public static boolean existsOperationInProgress(DecisionData data) {
            return AbstractTradingStrategy.existsOperationInProgress(data);
        }

        public int getAvailableLots(DecisionData data) {
            return super.getAvailableLots(data);
        }

        @NotNull
        @Override
        public StrategyCache initCache() {
            throw new NotImplementedException();
        }

    }

}