package ru.obukhov.trader.trading.bots;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.RealContext;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

@Slf4j
public class RunnableBot extends Bot implements Runnable {

    private final SchedulingProperties schedulingProperties;
    private final BotConfig botConfig;

    public RunnableBot(
            final ServicesContainer services,
            final RealContext realContext,
            final TradingStrategy strategy,
            final SchedulingProperties schedulingProperties,
            final BotConfig botConfig
    ) {
        super(services, realContext, strategy, strategy.initCache());

        this.schedulingProperties = schedulingProperties;
        this.botConfig = botConfig;
    }

    @Override
    public void run() {
        if (!schedulingProperties.isEnabled()) {
            log.trace("Scheduling is disabled. Do nothing");
            return;
        }

        final SecurityTradingStatus tradingStatus = extMarketDataService.getTradingStatus(botConfig.ticker());
        if (tradingStatus != SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING) {
            log.debug(
                    "Trading status fot ticker {} is {}. Expected {}",
                    botConfig.ticker(), tradingStatus, SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING
            );
            return;
        }

        try {
            processBotConfig(botConfig, null);
        } catch (final Exception exception) {
            log.error("Failed to process botConfig {}", botConfig, exception);
        }
    }

}