package ru.obukhov.trader.trading.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.trading.strategy.interfaces.StrategyCache;

@Data
@AllArgsConstructor
public class Decision {

    private final DecisionAction action;

    private final Long lots;

    private final StrategyCache strategyCache;

    public Decision(@NotNull DecisionAction action) {
        this(action, null, null);
    }

    public Decision(@NotNull DecisionAction action, @Nullable Long lots) {
        this(action, lots, null);
    }

    public String toPrettyString() {
        return switch (action) {
            case WAIT -> "Wait";
            case BUY -> "Buy " + lots + " lots";
            case SELL -> "Sell " + lots + " lots";
        };
    }

}