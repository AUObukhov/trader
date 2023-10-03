package ru.obukhov.trader.trading.bots;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
public class RunnableBot extends Bot implements Runnable {

    private final SchedulingProperties schedulingProperties;
    private final BotConfig botConfig;

    public RunnableBot(
            final ServicesContainer services,
            final Context context,
            final TradingStrategy strategy,
            final SchedulingProperties schedulingProperties,
            final BotConfig botConfig
    ) {
        super(services, context, strategy, strategy.initCache());

        this.schedulingProperties = schedulingProperties;
        this.botConfig = botConfig;
    }

    @Override
    public void run() {
        if (!schedulingProperties.isEnabled()) {
            log.trace("Scheduling is disabled. Do nothing");
            return;
        }

        final String figi = botConfig.figi();
        final SecurityTradingStatus tradingStatus = extMarketDataService.getTradingStatus(figi);
        if (tradingStatus != SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING) {
            log.debug(
                    "Trading status fot FIGI {} is {}. Expected {}",
                    figi, tradingStatus, SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING
            );
            return;
        }

        try {
            final OffsetDateTime currentDateTime = OffsetDateTime.now();
            final List<Candle> candles = extMarketDataService.getLastCandles(figi, LAST_CANDLES_COUNT, botConfig.candleInterval(), currentDateTime);
            processBotConfig(botConfig, candles);
        } catch (final Exception exception) {
            log.error("Failed to process botConfig {}", botConfig, exception);
        }
    }

}