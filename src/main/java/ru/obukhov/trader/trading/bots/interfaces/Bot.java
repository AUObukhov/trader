package ru.obukhov.trader.trading.bots.interfaces;

import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.web.model.BotConfig;

import java.time.OffsetDateTime;
import java.util.List;

public interface Bot {

    List<Candle> processBotConfig(final BotConfig botConfig, final OffsetDateTime previousStartTime);

}