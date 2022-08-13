package ru.obukhov.trader.trading.bots.impl;

import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtOperationsService;
import ru.obukhov.trader.market.impl.ExtOrdersService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.TinkoffServices;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.FakeContext;
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
    private final TinkoffServices tinkoffServices;
    private final ApplicationContext applicationContext;
    private final MarketDataService marketDataService;

    public FakeBot createBot(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final FakeContext fakeContext = createFakeContext(botConfig, balanceConfig, currentDateTime);
        final FakeTinkoffService fakeTinkoffService = (FakeTinkoffService) applicationContext.getBean(
                "fakeTinkoffService",
                marketProperties,
                tinkoffServices,
                fakeContext,
                botConfig.commission()
        );
        final ExtMarketDataService fakeExtMarketDataService = (ExtMarketDataService) applicationContext.getBean(
                "fakeExtMarketDataService",
                marketProperties,
                fakeTinkoffService,
                tinkoffServices.extInstrumentsService(),
                marketDataService
        );

        final ExtOperationsService fakeOperationsService = new ExtOperationsService(fakeTinkoffService);
        final ExtOrdersService fakeOrdersService = new ExtOrdersService(fakeTinkoffService, tinkoffServices.extInstrumentsService());
        final AbstractTradingStrategy strategy = strategyFactory.createStrategy(botConfig);

        return new FakeBot(
                fakeExtMarketDataService,
                tinkoffServices.extInstrumentsService(),
                fakeOperationsService,
                fakeOrdersService,
                fakeTinkoffService,
                strategy
        );
    }

    private FakeContext createFakeContext(final BotConfig botConfig, final BalanceConfig balanceConfig, final OffsetDateTime currentDateTime) {
        final OffsetDateTime ceilingWorkTime = DateUtils.getCeilingWorkTime(currentDateTime, marketProperties.getWorkSchedule());
        final Share share = tinkoffServices.extInstrumentsService().getShare(botConfig.ticker());
        if (share == null) {
            throw new IllegalArgumentException("Not found share for ticker '" + botConfig.ticker() + "'");
        }
        final Currency currency = Currency.valueOfIgnoreCase(share.getCurrency());

        final BigDecimal initialBalance = getInitialBalance(currentDateTime, ceilingWorkTime, balanceConfig);

        return new FakeContext(ceilingWorkTime, botConfig.accountId(), currency, initialBalance);
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