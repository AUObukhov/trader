package ru.obukhov.trader.trading.bots;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.MapUtils;
import ru.obukhov.trader.config.properties.SchedulingProperties;
import ru.obukhov.trader.market.impl.ServicesContainer;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
public class RunnableBot extends Bot implements Runnable {

    private final SchedulingProperties schedulingProperties;
    private final BotConfig botConfig;

    public RunnableBot(
            final ServicesContainer services,
            final Context context,
            final TradingStrategy strategy,
            final SchedulingProperties schedulingProperties,
            final BotConfig botConfig
    ) {
        super(services, context, strategy);

        this.schedulingProperties = schedulingProperties;
        this.botConfig = botConfig;
    }

    @Override
    public void run() {
        if (!schedulingProperties.isEnabled()) {
            log.trace("Scheduling is disabled. Do nothing");
            return;
        }

        if (checkTradingStatuses()) {
            final Interval interval = getInterval();

            try {
                processBotConfig(botConfig, interval);
            } catch (final Exception exception) {
                log.error("Failed to process botConfig {}", botConfig, exception);
            }
        }
    }

    private boolean checkTradingStatuses() {
        final SecurityTradingStatus expectedStatus = SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING;
        final Map<String, SecurityTradingStatus> unexpectedStatuses = botConfig.figies().stream()
                .collect(MapUtils.newMapValueCollector(extMarketDataService::getTradingStatus))
                .entrySet().stream()
                .filter(entry -> entry.getValue() != expectedStatus)
                .collect(MapUtils.newMapEntryCollector());
        if (unexpectedStatuses.isEmpty()) {
            return true;
        } else {
            log.debug("Expected trading status {} for all FIGIes. Unexpected statuses: {}", expectedStatus, unexpectedStatuses);
            return false;
        }
    }

    private Interval getInterval() {
        final OffsetDateTime to = DateUtils.now();
        final ChronoUnit period = DateUtils.getPeriodByCandleInterval(botConfig.candleInterval());
        final OffsetDateTime from = to.minus(period.getDuration());
        return Interval.of(from, to);
    }

}