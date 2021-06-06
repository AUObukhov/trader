package ru.obukhov.trader.bot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.bot.strategy.StrategyCache;

@Data
@AllArgsConstructor
public class Decision {

    private final DecisionAction action;

    private final Integer lots;

    private final StrategyCache strategyCache;

    public Decision(@NotNull DecisionAction action) {
        this(action, null, null);
    }

    public Decision(@NotNull DecisionAction action, @Nullable Integer lots) {
        this(action, lots, null);
    }

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