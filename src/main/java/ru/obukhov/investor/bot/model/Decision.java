package ru.obukhov.investor.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Decision {

    public static final Decision WAIT_DECISION = new Decision(DecisionAction.WAIT, null);

    DecisionAction action;

    Integer lots;

}