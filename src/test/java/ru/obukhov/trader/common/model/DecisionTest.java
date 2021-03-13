package ru.obukhov.trader.common.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecisionTest {

    @Test
    void toPrettyString_whenDecisionIsWait() {
        assertEquals("Wait", Decision.WAIT_DECISION.toPrettyString());
    }

    @Test
    void toPrettyString_whenDecisionIsBuy() {

        String prettyString = new Decision(DecisionAction.BUY, 100).toPrettyString();

        assertEquals("Buy 100 lots", prettyString);

    }

    @Test
    void toPrettyString_whenDecisionIsSell() {

        String prettyString = new Decision(DecisionAction.SELL, 100).toPrettyString();

        assertEquals("Sell 100 lots", prettyString);

    }

}