package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.OperationsService;
import ru.obukhov.trader.market.impl.RealTinkoffService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;

@Slf4j
public class ScheduledBot extends AbstractBot {

    private final SchedulingProperties schedulingProperties;
    private final BotConfig botConfig;
    private final MarketProperties marketProperties;

    public ScheduledBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final RealTinkoffService realTinkoffService,
            final TradingStrategy strategy,
            final SchedulingProperties schedulingProperties,
            final BotConfig botConfig,
            final MarketProperties marketProperties
    ) {
        super(marketService, operationsService, ordersService, portfolioService, realTinkoffService, strategy, strategy.initCache());

        this.schedulingProperties = schedulingProperties;
        this.botConfig = botConfig;
        this.marketProperties = marketProperties;
    }

    @Scheduled(fixedDelayString = "${scheduling.delay}")
    @SuppressWarnings("unused")
    public void tick() {
        if (!schedulingProperties.isEnabled()) {
            log.trace("Scheduling is disabled. Do nothing");
            return;
        }

        if (!DateUtils.isWorkTime(tinkoffService.getCurrentDateTime(), marketProperties.getWorkSchedule())) {
            log.debug("Not work time. Do nothing");
            return;
        }

        try {
            processBotConfig(botConfig, null);
        } catch (Exception exception) {
            log.error("Failed to process botConfig {}", botConfig, exception);
        }
    }

}