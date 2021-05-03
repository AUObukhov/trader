package ru.obukhov.trader.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Decision {

    public static final Decision WAIT_DECISION = new Decision(DecisionAction.WAIT, null);

    private final DecisionAction action;

    private final Integer lots;

    public String toPrettyString() {
        switch (action) {
            case WAIT:
                return "Wait";
            case BUY:
                return "Buy " + lots + " lots";
            case SELL:
                return "Sell " + lots + " lots";
            default:
                throw new IllegalStateException("Unknown action: " + action);
        }
    }

}