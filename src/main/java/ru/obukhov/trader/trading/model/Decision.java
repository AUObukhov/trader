package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;

@Data
@AllArgsConstructor
public class Decision {

    private final DecisionAction action;

    @Nullable
    private final Long quantity;

    @Nullable
    private final StrategyCache strategyCache;

    public Decision(DecisionAction action) {
        this(action, null, null);
    }

    public Decision(DecisionAction action, @Nullable Long quantity) {
        this(action, quantity, null);
    }

    public String toPrettyString() {
        return switch (action) {
            case WAIT -> "Wait";
            case BUY -> "Buy " + quantity + " securities";
            case SELL -> "Sell " + quantity + " securities";
        };
    }

}