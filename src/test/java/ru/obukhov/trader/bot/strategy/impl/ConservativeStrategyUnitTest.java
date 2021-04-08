package ru.obukhov.trader.bot.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.util.Arrays;

class ConservativeStrategyUnitTest {

    private static final double COMMISSION = 0.003;
    private static final TradingProperties TRADING_PROPERTIES = new TradingProperties();

    private final ConservativeStrategy strategy = new ConservativeStrategy(TRADING_PROPERTIES);

    @BeforeAll
    static void setUp() {
        TRADING_PROPERTIES.setCommission(COMMISSION);
    }

    @Test
    void decide_returnsWait_whenExistsOperationInProgress() {
        Operation operation1 = new Operation().status(OperationStatus.DONE);
        Operation operation2 = new Operation().status(OperationStatus.PROGRESS);
        Operation operation3 = new Operation().status(OperationStatus.DECLINE);

        DecisionData data = new DecisionData()
                .withLastOperations(Arrays.asList(operation1, operation2, operation3));

        Decision decision = strategy.decide(data);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    void decide_returnsWait_whenNoAvailableLots() {
        DecisionData data = TestDataHelper.createDecisionData(2000, 2000, 1);

        Decision decision = strategy.decide(data);

        Assertions.assertEquals(DecisionAction.WAIT, decision.getAction());
        Assertions.assertNull(decision.getLots());
    }

    @Test
    void decide_returnsBuy_whenThereAreAvailableLots() {
        DecisionData data = TestDataHelper.createDecisionData(10000, 2000, 1);

        Decision decision = strategy.decide(data);

        Assertions.assertEquals(DecisionAction.BUY, decision.getAction());
        Assertions.assertEquals(4, decision.getLots());
    }

}