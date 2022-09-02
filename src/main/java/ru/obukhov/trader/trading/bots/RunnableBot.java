package ru.obukhov.trader.trading.bots;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;

@Slf4j
public class RunnableBot extends Bot implements Runnable {

    private final SchedulingProperties schedulingProperties;
    private final BotConfig botConfig;
    private final MarketProperties marketProperties;

    public RunnableBot(
            final TinkoffServices tinkoffServices,
            final TradingStrategy strategy,
            final SchedulingProperties schedulingProperties,
            final BotConfig botConfig,
            final MarketProperties marketProperties
    ) {
        super(tinkoffServices, strategy, strategy.initCache());

        this.schedulingProperties = schedulingProperties;
        this.botConfig = botConfig;
        this.marketProperties = marketProperties;
    }

    @Override
    public void run() {
        if (!schedulingProperties.isEnabled()) {
            log.trace("Scheduling is disabled. Do nothing");
            return;
        }

        if (!DateUtils.isWorkTime(context.getCurrentDateTime(), marketProperties.getWorkSchedule())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        try {
            processBotConfig(botConfig, null);
        } catch (final Exception exception) {
            log.error("Failed to process botConfig {}", botConfig, exception);
        }
    }

}