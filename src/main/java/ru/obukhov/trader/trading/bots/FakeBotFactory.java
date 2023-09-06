package ru.obukhov.trader.trading.bots;

import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.Asserter;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.TradingDayUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.impl.FakeExtOperationsService;
import ru.obukhov.trader.market.impl.FakeExtOrdersService;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

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
                fakeContext,
                strategy
        );
    }

    private FakeContext createFakeContext(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final String figi = botConfig.figi();
        final OffsetDateTime ceilingWorkDateTime = getCeilingWorkDateTime(figi, currentDateTime);
        final Share share = extInstrumentsService.getShare(figi);
        Asserter.notNull(share, () -> new InstrumentNotFoundException(figi));
        final BigDecimal initialBalance = getInitialBalance(currentDateTime, ceilingWorkDateTime, balanceConfig);

        return (FakeContext) applicationContext.getBean(
                "fakeContext",
                ceilingWorkDateTime,
                botConfig.accountId(),
                share.currency(),
                initialBalance
        );
    }

    private OffsetDateTime getCeilingWorkDateTime(final String figi, final OffsetDateTime currentDateTime) {
        final Interval interval = Interval.of(currentDateTime, currentDateTime.plusDays(SCHEDULE_INTERVAL_DAYS));
        final List<TradingDay> tradingSchedule = extInstrumentsService.getTradingScheduleByFigi(figi, interval);
        return TradingDayUtils.ceilingScheduleMinute(tradingSchedule, currentDateTime);
    }

    private BigDecimal getInitialBalance(
            final OffsetDateTime currentDateTime,
            final OffsetDateTime ceilingWorkDateTime,
            final BalanceConfig balanceConfig
    ) {
        BigDecimal initialBalance = balanceConfig.getInitialBalance() == null ? DecimalUtils.setDefaultScale(0) : balanceConfig.getInitialBalance();

        // adding balance increments which were skipped by moving to ceiling work time above
        final CronExpression balanceIncrementCron = balanceConfig.getBalanceIncrementCron();
        if (balanceIncrementCron != null) {
            final int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, currentDateTime, ceilingWorkDateTime)
                    .size();
            if (incrementsCount > 0) {
                final BigDecimal totalBalanceIncrement = DecimalUtils.multiply(balanceConfig.getBalanceIncrement(), incrementsCount);
                initialBalance = initialBalance.add(totalBalanceIncrement);
            }
        }
        return initialBalance;
    }

}