package ru.obukhov.trader.trading.bots.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.web.model.BotConfig;

import java.time.OffsetDateTime;

public interface Bot {

    @NotNull
    DecisionData processBotConfig(final BotConfig botConfig, final OffsetDateTime previousStartTime, final OffsetDateTime now);

}