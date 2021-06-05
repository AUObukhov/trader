package ru.obukhov.trader.bot.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.bot.model.DecisionData;

import java.time.OffsetDateTime;

public interface Bot {

    @NotNull
    DecisionData processTicker(final String ticker, final OffsetDateTime previousStartTime);

}