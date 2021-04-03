package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;

class DecisionUnitTest {

    @Test
    void toPrettyString_whenDecisionIsWait() {
        Assertions.assertEquals("Wait", Decision.WAIT_DECISION.toPrettyString());
    }

    @Test
    void toPrettyString_whenDecisionIsBuy() {

        String prettyString = new Decision(DecisionAction.BUY, 100).toPrettyString();

        Assertions.assertEquals("Buy 100 lots", prettyString);

    }

    @Test
    void toPrettyString_whenDecisionIsSell() {

        String prettyString = new Decision(DecisionAction.SELL, 100).toPrettyString();

        Assertions.assertEquals("Sell 100 lots", prettyString);

    }

}