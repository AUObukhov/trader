package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;

@Slf4j
public class ScheduledBot extends AbstractBot {

    private final ScheduledBotProperties scheduledBotProperties;
    private final MarketProperties marketProperties;

    public ScheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final RealTinkoffService realTinkoffService,
            final TradingStrategy strategy,
            final ScheduledBotProperties scheduledBotProperties,
            final MarketProperties marketProperties
    ) {
        super(marketService, operationsService, ordersService, portfolioService, realTinkoffService, strategy, strategy.initCache());

        this.scheduledBotProperties = scheduledBotProperties;
        this.marketProperties = marketProperties;
    }

    @Scheduled(fixedDelayString = "${scheduled-bot.delay}")
    @SuppressWarnings("unused")
    public void tick() {
        if (!scheduledBotProperties.isEnabled()) {
            log.trace("Bot is disabled. Do nothing");
            return;
        }

        if (!DateUtils.isWorkTimeNow(marketProperties.getWorkStartTime(), marketProperties.getWorkDuration())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        processBotConfigSafe(scheduledBotProperties.getBotConfig());
    }

    public void processBotConfigSafe(final BotConfig botConfig) {
        try {
            processBotConfig(botConfig, null);
        } catch (Exception exception) {
            log.error("Failed to process botConfig {}", botConfig, exception);
        }
    }

}