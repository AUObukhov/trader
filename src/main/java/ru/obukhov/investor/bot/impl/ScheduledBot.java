package ru.obukhov.investor.bot.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.investor.bot.interfaces.Bot;
import ru.obukhov.investor.bot.interfaces.DataSupplier;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.config.BotProperties;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.obukhov.investor.util.DateUtils;

@Slf4j
public class ScheduledBot extends SimpleBot implements Bot {

    private final BotProperties botProperties;
    private final TradingProperties tradingProperties;

    public ScheduledBot(DataSupplier dataSupplier,
                        Decider decider,
                        OrdersService ordersService,
                        BotProperties botProperties,
                        TradingProperties tradingProperties) {

        super(dataSupplier, decider, ordersService);

        this.botProperties = botProperties;
        this.tradingProperties = tradingProperties;
    }

    @Scheduled(fixedDelayString = "${bot.delay}")
    public void tick() {
        if (!DateUtils.isWorkTimeNow(tradingProperties.getWorkStartTime(), tradingProperties.getWorkDuration())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        botProperties.getTickers().forEach(this::processTicker);
    }

}