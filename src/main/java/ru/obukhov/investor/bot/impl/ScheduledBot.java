package ru.obukhov.investor.bot.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.config.BotProperties;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.util.DateUtils;

@Slf4j
public class ScheduledBot extends SimpleBot {

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