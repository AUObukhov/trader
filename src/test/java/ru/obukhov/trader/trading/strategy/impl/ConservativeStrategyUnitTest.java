package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.List;

class ConservativeStrategyUnitTest {

    private final ConservativeStrategy strategy = TestData.CONSERVATIVE_STRATEGY;

    @Test
    void getName_returnsProperName() {
        Assertions.assertEquals("conservative", strategy.getName());
    }

    // region decide tests

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
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
    void decide_returnsWait_whenNoAvailableLots() {
        final DecisionData data = TestData.createDecisionData(2000.0, 2000.0, 1, 0.003);

        final Decision decision = strategy.decide(data, strategy.initCache());

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    void decide_returnsBuy_whenThereAreAvailableLots() {
        final DecisionData data = TestData.createDecisionData(10000.0, 2000.0, 1, 0.003);

        final Decision decision = strategy.decide(data, strategy.initCache());

        Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
        Assertions.assertEquals(4, decision.getLots());
    }

    // endregion

    @Test
    void initCache_returnsNotNull() {
        Assertions.assertNotNull(strategy.initCache());
    }

}