package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.ScheduledBotProperties;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ScheduledBot extends AbstractBot {

    private final ScheduledBotProperties scheduledBotProperties;
    private final TradingProperties tradingProperties;

    public ScheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final TradingStrategy strategy,
            final ScheduledBotProperties scheduledBotProperties,
            final TradingProperties tradingProperties
    ) {
        super(
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategy,
                strategy.initCache(),
                scheduledBotProperties.getCandleResolution()
        );

        this.scheduledBotProperties = scheduledBotProperties;
        this.tradingProperties = tradingProperties;
    }

    @Scheduled(fixedDelayString = "${scheduled-bot.delay}")
    @SuppressWarnings("unused")
    public void tick() {
        if (!scheduledBotProperties.isEnabled()) {
            log.trace("Bot is disabled. Do nothing");
            return;
        }

        if (!DateUtils.isWorkTimeNow(tradingProperties.getWorkStartTime(), tradingProperties.getWorkDuration())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        final Set<String> tickers = new HashSet<>(scheduledBotProperties.getTickers());
        if (tickers.isEmpty()) {
            log.warn("No tickers configured for bot. Do nothing");
            return;
        }

        tickers.forEach(ticker -> processTickerSafe(scheduledBotProperties.getBrokerAccountId(), ticker, null));
    }

    public void processTickerSafe(@Nullable final String brokerAccountId, final String ticker, final OffsetDateTime previousStartTime) {
        try {
            processTicker(brokerAccountId, ticker, previousStartTime, OffsetDateTime.now());
        } catch (Exception exception) {
            log.error("Failed to process ticker '{}'", ticker, exception);
        }
    }

}