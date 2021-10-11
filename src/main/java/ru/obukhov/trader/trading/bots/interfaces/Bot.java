package ru.obukhov.trader.trading.bots.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.OffsetDateTime;

public interface Bot {

    @NotNull
    DecisionData processTicker(
            @Nullable final String brokerAccountId,
            final String ticker,
            final CandleResolution candleResolution,
            final OffsetDateTime previousStartTime,
            final OffsetDateTime now
    );

}