package ru.obukhov.trader.trading.bots;

import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.impl.FakeExtOperationsService;
import ru.obukhov.trader.market.impl.FakeExtOrdersService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.trading.strategy.impl.AbstractTradingStrategy;
import ru.obukhov.trader.trading.strategy.impl.TradingStrategyFactory;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.obukhov.trader.web.model.BotConfig;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.MarketDataService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class FakeBotFactory {

    private final MarketProperties marketProperties;
    private final TradingStrategyFactory strategyFactory;
    private final ExtInstrumentsService extInstrumentsService;
    private final ApplicationContext applicationContext;
    private final MarketDataService marketDataService;

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final FakeContext fakeContext = createFakeContext(botConfig, balanceConfig, currentDateTime);
        final ExtMarketDataService fakeExtMarketDataService = (ExtMarketDataService) applicationContext.getBean(
                "fakeExtMarketDataService",
                marketProperties,
                fakeContext,
                extInstrumentsService,
                marketDataService
        );

        final ExtOperationsService fakeOperationsService = new FakeExtOperationsService(fakeContext);
        final FakeExtOrdersService fakeOrdersService = new FakeExtOrdersService(
                fakeContext,
                extInstrumentsService,
                fakeExtMarketDataService,
                botConfig.commission()
        );
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(
                fakeExtMarketDataService,
                extInstrumentsService,
                fakeOperationsService,
                fakeOrdersService,
                fakeContext,
                strategy
        );
    }

    private FakeContext createFakeContext(
            final BotConfig botConfig,
            final BalanceConfig balanceConfig,
            final OffsetDateTime currentDateTime
    ) {
        final OffsetDateTime ceilingWorkTime = DateUtils.getCeilingWorkTime(currentDateTime, marketProperties.getWorkSchedule());
        final Share share = extInstrumentsService.getShare(botConfig.ticker());
        if (share == null) {
            throw new IllegalArgumentException("Not found share for ticker '" + botConfig.ticker() + "'");
        }
        final Currency currency = Currency.valueOfIgnoreCase(share.getCurrency());

        final BigDecimal initialBalance = getInitialBalance(currentDateTime, ceilingWorkTime, balanceConfig);

        return (FakeContext) applicationContext.getBean(
                "fakeContext",
                marketProperties,
                ceilingWorkTime,
                botConfig.accountId(),
                currency,
                initialBalance
        );
    }

    private BigDecimal getInitialBalance(
            final OffsetDateTime currentDateTime,
            final OffsetDateTime ceilingWorkTime,
            final BalanceConfig balanceConfig
    ) {
        BigDecimal initialBalance = balanceConfig.getInitialBalance() == null ? BigDecimal.ZERO : balanceConfig.getInitialBalance();

        // adding balance increments which were skipped by moving to ceiling work time above
        final CronExpression balanceIncrementCron = balanceConfig.getBalanceIncrementCron();
        if (balanceIncrementCron != null) {
            final int incrementsCount = DateUtils.getCronHitsBetweenDates(balanceIncrementCron, currentDateTime, ceilingWorkTime)
                    .size();
            if (incrementsCount > 0) {
                final BigDecimal totalBalanceIncrement = DecimalUtils.multiply(balanceConfig.getBalanceIncrement(), incrementsCount);
                initialBalance = initialBalance.add(totalBalanceIncrement);
            }
        }
        return initialBalance;
    }

}