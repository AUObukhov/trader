package ru.obukhov.trader.bot.strategy;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.Arrays;
import java.util.Collections;

class AbstractStrategyUnitTest {

    private static final double COMMISSION = 0.003;
    private static final TradingProperties TRADING_PROPERTIES = new TradingProperties();

    private final AbstractStrategy strategy = new TestStrategy(TRADING_PROPERTIES);

    @BeforeAll
    static void setUp() {
        TRADING_PROPERTIES.setCommission(COMMISSION);
    }

    @ParameterizedTest
    @CsvSource({
            "1000.0, 1100.0, 0.09342",
            "1000.0, 900.0, -0.10538",
    })
    void getProfit(double averagePositionPrice, double currentPrice, double expectedProfit) {
        DecisionData data = TestDataHelper.createDecisionData(averagePositionPrice, currentPrice);

        double profit = strategy.getProfit(data);

        Assertions.assertEquals(expectedProfit, profit);
    }

    // region existsOperationInProgress tests

    @Test
    void existsOperationInProgress_returnsTrue_whenOperationInProgressExists() {
        Operation operation1 = new Operation().status(OperationStatus.DONE);
        Operation operation2 = new Operation().status(OperationStatus.PROGRESS);
        Operation operation3 = new Operation().status(OperationStatus.DECLINE);

        DecisionData data = new DecisionData().withLastOperations(Arrays.asList(operation1, operation2, operation3));

        Assertions.assertTrue(TestStrategy.existsOperationInProgress(data));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenOperationInProgressDoesNotExists() {
        Operation operation1 = new Operation().status(OperationStatus.DONE);
        Operation operation2 = new Operation().status(OperationStatus.DECLINE);

        DecisionData data = new DecisionData().withLastOperations(Arrays.asList(operation1, operation2));

        Assertions.assertFalse(TestStrategy.existsOperationInProgress(data));
    }

    @Test
    void existsOperationInProgress_returnsFalse_whenNoOperations() {
        DecisionData data = new DecisionData().withLastOperations(Collections.emptyList());

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
        DecisionData data = TestDataHelper.createDecisionData(balance, currentPrice, lotSize);

        int availableLots = strategy.getAvailableLots(data);

        Assertions.assertEquals(expectedAvailableLots, availableLots);
    }

    private static class TestStrategy extends AbstractStrategy {

        public TestStrategy(TradingProperties tradingProperties) {
            super(tradingProperties);
        }

        @Override
        public Decision decide(DecisionData data) {
            throw new NotImplementedException();
        }

        public double getProfit(DecisionData data) {
            return super.getProfit(data);
        }

        public static boolean existsOperationInProgress(DecisionData data) {
            return AbstractStrategy.existsOperationInProgress(data);
        }

        public int getAvailableLots(DecisionData data) {
            return super.getAvailableLots(data);
        }

    }

}