package ru.obukhov.trader.trading.bots.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.BalanceConfig;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.SortedMap;

@Slf4j
public class FakeBot extends AbstractBot implements Bot {

    @Getter
    private final FakeTinkoffService fakeTinkoffService;

    public FakeBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final TradingStrategy strategy,
            final FakeTinkoffService fakeTinkoffService
    ) {
        super(marketService, operationsService, ordersService, portfolioService, strategy, strategy.initCache());

        this.fakeTinkoffService = fakeTinkoffService;
    }

    // region FakeTinkoffService proxy

    public void init(
            @Nullable final String brokerAccountId,
            final OffsetDateTime currentDateTime,
            @Nullable final Currency currency,
            final BalanceConfig balanceConfig
    ) {
        fakeTinkoffService.init(brokerAccountId, currentDateTime, currency, balanceConfig);
    }

    public MarketInstrument searchMarketInstrument(final String ticker) {
        return fakeTinkoffService.searchMarketInstrument(ticker);
    }

    public OffsetDateTime getCurrentDateTime() {
        return fakeTinkoffService.getCurrentDateTime();
    }

    public OffsetDateTime nextMinute() {
        return fakeTinkoffService.nextMinute();
    }

    public void addInvestment(final String brokerAccountId, final OffsetDateTime dateTime, final Currency currency, final BigDecimal increment) {
        fakeTinkoffService.addInvestment(brokerAccountId, dateTime, currency, increment);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final Currency currency) {
        return fakeTinkoffService.getInvestments(currency);
    }

    public BigDecimal getCurrentBalance(@Nullable final String brokerAccountId, final Currency currency) {
        return fakeTinkoffService.getCurrentBalance(brokerAccountId, currency);
    }

    public List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, @Nullable final String ticker) {
        return fakeTinkoffService.getOperations(brokerAccountId, interval, ticker);
    }

    public List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) {
        return fakeTinkoffService.getPortfolioPositions(brokerAccountId);
    }

    public BigDecimal getCurrentPrice(final String ticker) {
        return fakeTinkoffService.getCurrentPrice(ticker);
    }

    // endregion

}