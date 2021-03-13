package ru.obukhov.trader.bot.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.trader.bot.interfaces.Decider;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.BotProperties;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;

@Slf4j
public class ScheduledBot extends AbstractBot {

    private final BotProperties botProperties;
    private final TradingProperties tradingProperties;

    public ScheduledBot(Decider decider,
                        MarketService marketService,
                        OperationsService operationsService,
                        OrdersService ordersService,
                        PortfolioService portfolioService,
                        BotProperties botProperties,
                        TradingProperties tradingProperties) {

        super(decider, marketService, operationsService, ordersService, portfolioService);

        this.botProperties = botProperties;
        this.tradingProperties = tradingProperties;
    }

    @Scheduled(fixedDelayString = "${bot.delay}")
    public void tick() {
        if (!tradingProperties.isBotEnabled()) {
            log.trace("Bot is disabled. Do nothing");
            return;
        }

        if (!DateUtils.isWorkTimeNow(tradingProperties.getWorkStartTime(), tradingProperties.getWorkDuration())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        botProperties.getTickers().forEach(this::processTicker);
    }

}