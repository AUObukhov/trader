package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;

import java.time.OffsetDateTime;

@Slf4j
public class ScheduledBot extends AbstractBot {

    private final ScheduledBotProperties scheduledBotProperties;
    private final MarketProperties marketProperties;

    public ScheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final TradingStrategy strategy,
            final ScheduledBotProperties scheduledBotProperties,
            final MarketProperties marketProperties
    ) {
        super(
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategy,
                strategy.initCache(),
                scheduledBotProperties.getBotConfig().getCandleResolution()
        );

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

        final BotConfig botConfig = scheduledBotProperties.getBotConfig();

        processTickerSafe(botConfig.getBrokerAccountId(), botConfig.getTicker(), null);
    }

    public void processTickerSafe(@Nullable final String brokerAccountId, final String ticker, final OffsetDateTime previousStartTime) {
        try {
            processTicker(brokerAccountId, ticker, previousStartTime, OffsetDateTime.now());
        } catch (Exception exception) {
            log.error("Failed to process ticker '{}'", ticker, exception);
        }
    }

}