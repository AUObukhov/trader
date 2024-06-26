package ru.obukhov.trader.trading.bots;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.Asserter;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TradingDayUtils;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.impl.FakeExtOperationsService;
import ru.obukhov.trader.market.impl.FakeExtOrdersService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FakeBotFactory {

    /**
     * Number of sequential days that are guaranteed to contain at least one trading day whenever they are in the year
     */
    private static final int SCHEDULE_INTERVAL_DAYS = 5;

    private final TradingStrategyFactory strategyFactory;
    private final ExtMarketDataService extMarketDataService;
    private final ExtInstrumentsService extInstrumentsService;
    private final ExtUsersService extUsersService;
    private final ApplicationContext applicationContext;

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final FakeContext fakeContext = createFakeContext(botConfig, balanceConfig, currentDateTime);
        final ExtOperationsService fakeOperationsService = new FakeExtOperationsService(fakeContext);
        final FakeExtOrdersService fakeOrdersService = new FakeExtOrdersService(
                fakeContext,
                extInstrumentsService,
                extMarketDataService,
                botConfig.commission()
        );
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(
                extMarketDataService,
                extInstrumentsService,
                fakeOperationsService,
                fakeOrdersService,
                extUsersService,
                fakeContext,
                strategy
        );
    }

    private FakeContext createFakeContext(
            final BotConfig botConfig,
            final BalanceConfig balanceConfig,
            final OffsetDateTime currentDateTime
    ) {
        final List<String> figies = botConfig.figies();
        final OffsetDateTime ceilingWorkDateTime = getCeilingWorkDateTime(figies, currentDateTime);
        final List<Share> shares = extInstrumentsService.getShares(figies);
        final List<String> notFoundFigies = figies.stream()
                .filter(figi -> shares.stream().noneMatch(share -> share.figi().equals(figi)))
                .toList();
        Asserter.isTrue(notFoundFigies.isEmpty(), () -> new InstrumentNotFoundException(notFoundFigies));
        final Map<String, BigDecimal> initialBalances = getInitialBalances(currentDateTime, ceilingWorkDateTime, balanceConfig);

        return (FakeContext) applicationContext.getBean("fakeContext", botConfig.accountId(), ceilingWorkDateTime, initialBalances);
    }

    private OffsetDateTime getCeilingWorkDateTime(final List<String> figies, final OffsetDateTime currentDateTime) {
        final Interval interval = Interval.of(currentDateTime, currentDateTime.plusDays(SCHEDULE_INTERVAL_DAYS));
        final List<TradingDay> tradingSchedule = figies.stream()
                .map(figi -> extInstrumentsService.getTradingScheduleByFigi(figi, interval))
                .min(Comparator.comparing(schedule -> schedule.getFirst().startTime()))
                .orElseThrow();
        return TradingDayUtils.ceilingScheduleMinute(tradingSchedule, currentDateTime);
    }

    private Map<String, BigDecimal> getInitialBalances(
            final OffsetDateTime currentDateTime,
            final OffsetDateTime ceilingWorkDateTime,
            final BalanceConfig balanceConfig
    ) {
        final Map<String, BigDecimal> initialBalances = balanceConfig.getInitialBalances();

        // adding balance increments which were skipped by moving to ceiling work time above
        final CronExpression balanceIncrementCron = balanceConfig.getBalanceIncrementCron();
        if (balanceConfig.getBalanceIncrements() != null && !currentDateTime.isEqual(ceilingWorkDateTime)) {
            final int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, currentDateTime, ceilingWorkDateTime)
                    .size();
            for (final Map.Entry<String, BigDecimal> entry : balanceConfig.getBalanceIncrements().entrySet()) {
                final String currency = entry.getKey();
                final BigDecimal totalBalanceIncrement = DecimalUtils.multiply(entry.getValue(), incrementsCount);
                final BigDecimal newBalanceValue = initialBalances.get(currency).add(totalBalanceIncrement);
                initialBalances.put(currency, newBalanceValue);
            }
        }
        return initialBalances;
    }

}