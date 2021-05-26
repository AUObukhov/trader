package ru.obukhov.trader.bot.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class ScheduledBot extends AbstractBot {

    private final BotConfig botConfig;
    private final TradingProperties tradingProperties;

    public ScheduledBot(
            TradingStrategy strategy,
            MarketService marketService,
            OperationsService operationsService,
            OrdersService ordersService,
            PortfolioService portfolioService,
            BotConfig botConfig,
            TradingProperties tradingProperties
    ) {
        super(strategy, marketService, operationsService, ordersService, portfolioService);

        this.botConfig = botConfig;
        this.tradingProperties = tradingProperties;
    }

    @Scheduled(fixedDelayString = "${bot.delay}")
    @SuppressWarnings("unused")
    public void tick() {
        if (!botConfig.isEnabled()) {
            log.trace("Bot is disabled. Do nothing");
            return;
        }

        if (!DateUtils.isWorkTimeNow(tradingProperties.getWorkStartTime(), tradingProperties.getWorkDuration())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        Set<String> tickers = new HashSet<>(botConfig.getTickers());
        if (tickers.isEmpty()) {
            log.warn("No tickers configured for bot. Do nothing");
            return;
        }

        tickers.forEach(ticker -> processTickerSafe(ticker, null));
    }

    public void processTickerSafe(String ticker, OffsetDateTime previousStartTime) {
        try {
            processTicker(ticker, previousStartTime);
        } catch (Exception exception) {
            log.error("Failed to process ticker '{}'", ticker, exception);
        }
    }

}