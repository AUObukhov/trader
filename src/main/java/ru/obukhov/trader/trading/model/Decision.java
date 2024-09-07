package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class Decision {

    private final DecisionAction action;

    @Nullable
    private final Long quantity;

    public Decision(DecisionAction action) {
        this(action, null);
    }

    public String toPrettyString() {
        return switch (action) {
            case WAIT -> "Wait";
            case BUY -> "Buy " + quantity + " securities";
            case SELL -> "Sell " + quantity + " securities";
        };
    }

}