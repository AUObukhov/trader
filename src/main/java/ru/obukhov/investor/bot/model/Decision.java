package ru.obukhov.investor.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Decision {

    public static final Decision WAIT_DECISION = new Decision(DecisionAction.WAIT, null);

    private final DecisionAction action;

    private final Integer lots;

    public String toPrettyString() {
        if (action == DecisionAction.WAIT) {
            return "Wait";
        } else if (action == DecisionAction.BUY) {
            return "Buy " + lots + " lots";
        } else {
            return "Sell " + lots + " lots";
        }
    }

}