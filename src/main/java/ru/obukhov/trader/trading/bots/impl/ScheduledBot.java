package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotConfig;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ScheduledBot extends AbstractBot {

    private final BotConfig botConfig;
    private final TradingProperties tradingProperties;

    public ScheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final TradingStrategy strategy,
            final CandleResolution candleResolution,
            final BotConfig botConfig,
            final TradingProperties tradingProperties
    ) {
        super(
                marketService,
                operationsService,
                ordersService,
                portfolioService,
                strategy,
                strategy.initCache(),
                candleResolution
        );

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

        final Set<String> tickers = new HashSet<>(botConfig.getTickers());
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