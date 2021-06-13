package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.List;

class ConservativeStrategyUnitTest {

    private static final TradingProperties TRADING_PROPERTIES = new TradingProperties();

    private final ConservativeStrategy strategy = new ConservativeStrategy(0.1f, TRADING_PROPERTIES);

    @BeforeAll
    static void setUp() {
        TRADING_PROPERTIES.setCommission(0.003);
    }

    @Test
    void getName_returnsProperName() {
        Assertions.assertEquals("Conservative (0.1)", strategy.getName());
    }

    // region decide tests

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        final Operation operation1 = new Operation().status(OperationStatus.DONE);
        final Operation operation2 = new Operation().status(OperationStatus.PROGRESS);
        final Operation operation3 = new Operation().status(OperationStatus.DECLINE);

        final DecisionData data = new DecisionData();
        data.setLastOperations(List.of(operation1, operation2, operation3));

        final Decision decision = strategy.decide(data, null);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    void decide_returnsWait_whenNoAvailableLots() {
        final DecisionData data = TestDataHelper.createDecisionData(2000.0, 2000.0, 1);

        final Decision decision = strategy.decide(data, null);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    void decide_returnsBuy_whenThereAreAvailableLots() {
        final DecisionData data = TestDataHelper.createDecisionData(10000.0, 2000.0, 1);

        final Decision decision = strategy.decide(data, null);

        Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
        Assertions.assertEquals(4, decision.getLots());
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        Assertions.assertNotNull(strategy.initCache());
    }

}