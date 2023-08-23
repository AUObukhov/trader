package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;

class DecisionUnitTest {

    @Test
    void toPrettyString_whenDecisionIsWait() {
        String prettyString = new Decision(DecisionAction.WAIT).toPrettyString();

        Assertions.assertEquals("Wait", prettyString);
    }

    @Test
    void toPrettyString_whenDecisionIsBuy() {
        final String prettyString = new Decision(DecisionAction.BUY, 100L).toPrettyString();

        Assertions.assertEquals("Buy 100 securities", prettyString);
    }

    @Test
    void toPrettyString_whenDecisionIsSell() {
        final String prettyString = new Decision(DecisionAction.SELL, 100L).toPrettyString();

        Assertions.assertEquals("Sell 100 securities", prettyString);
    }

}