package ru.obukhov.investor.model;

import org.junit.Test;
import ru.obukhov.investor.bot.model.Decision;
import ru.obukhov.investor.bot.model.DecisionAction;

import static org.junit.Assert.assertEquals;

public class DecisionTest {

    @Test
    public void toPrettyString_whenDecisionIsWait() {
        assertEquals("Wait", Decision.WAIT_DECISION.toPrettyString());
    }

    @Test
    public void toPrettyString_whenDecisionIsBuy() {

        String prettyString = new Decision(DecisionAction.BUY, 100).toPrettyString();

        assertEquals("Buy 100 lots", prettyString);

    }

    @Test
    public void toPrettyString_whenDecisionIsSell() {

        String prettyString = new Decision(DecisionAction.SELL, 100).toPrettyString();

        assertEquals("Sell 100 lots", prettyString);

    }

}