package ru.obukhov.trader.trading.bots;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.common.util.TradingDayUtils;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.impl.FakeExtOperationsService;
import ru.obukhov.trader.market.impl.FakeExtOrdersService;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.TradingDay;

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

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final Timestamp currentTimestamp) {
        final FakeContext fakeContext = createFakeContext(botConfig, balanceConfig, currentTimestamp);
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

    private FakeContext createFakeContext(final BotConfig botConfig, final BalanceConfig balanceConfig, final Timestamp currentTimestamp) {
        final Timestamp ceilingWorkTimestamp = getCeilingWorkTimestamp(botConfig.figi(), currentTimestamp);
        final Share share = extInstrumentsService.getShare(botConfig.figi());
        if (share == null) {
            throw new IllegalArgumentException("Not found share for FIGI '" + botConfig.figi() + "'");
        }
        final Quotation initialBalance = getInitialBalance(currentTimestamp, ceilingWorkTimestamp, balanceConfig);

        return (FakeContext) applicationContext.getBean(
                "fakeContext",
                ceilingWorkTimestamp,
                botConfig.accountId(),
                share.getCurrency(),
                initialBalance
        );
    }

    private Timestamp getCeilingWorkTimestamp(final String figi, final Timestamp currentTimestamp) {
        final Interval interval = Interval.of(currentTimestamp, TimestampUtils.plusDays(currentTimestamp, SCHEDULE_INTERVAL_DAYS));
        final List<TradingDay> tradingSchedule = extInstrumentsService.getTradingScheduleByFigi(figi, interval);
        return TradingDayUtils.ceilingScheduleMinute(tradingSchedule, currentTimestamp);
    }

    private Quotation getInitialBalance(
            final Timestamp currentTimestamp,
            final Timestamp ceilingWorkTimestamp,
            final BalanceConfig balanceConfig
    ) {
        Quotation initialBalance = balanceConfig.getInitialBalance() == null ? QuotationUtils.ZERO : balanceConfig.getInitialBalance();

        // adding balance increments which were skipped by moving to ceiling work time above
        final CronExpression balanceIncrementCron = balanceConfig.getBalanceIncrementCron();
        if (balanceIncrementCron != null) {
            final int incrementsCount = TimestampUtils.getCronHitsBetweenDates(balanceIncrementCron, currentTimestamp, ceilingWorkTimestamp)
                    .size();
            if (incrementsCount > 0) {
                final Quotation totalBalanceIncrement = QuotationUtils.multiply(balanceConfig.getBalanceIncrement(), incrementsCount);
                initialBalance = QuotationUtils.add(initialBalance, totalBalanceIncrement);
            }
        }
        return initialBalance;
    }

}