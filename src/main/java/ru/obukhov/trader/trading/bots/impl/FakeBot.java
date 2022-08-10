package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.SortedMap;

@Slf4j
public class FakeBot extends AbstractBot implements Bot {

    public FakeBot(
            final ExtMarketDataService extMarketDataService,
            final MarketInstrumentsService marketInstrumentsService,
            final MarketOperationsService operationsService,
            final MarketOrdersService ordersService,
            final PortfolioService portfolioService,
            final FakeTinkoffService fakeTinkoffService,
            final TradingStrategy strategy
    ) {
        super(
                extMarketDataService,
                marketInstrumentsService,
                operationsService,
                ordersService,
                portfolioService,
                fakeTinkoffService,
                strategy,
                strategy.initCache()
        );
    }

    public FakeTinkoffService getFakeTinkoffService() {
        return (FakeTinkoffService) tinkoffService;
    }

    // region FakeTinkoffService proxy

    public Share getShare(final String ticker) {
        return marketInstrumentsService.getShare(ticker);
    }

    public OffsetDateTime getCurrentDateTime() {
        return tinkoffService.getCurrentDateTime();
    }

    public List<Operation> getOperations(final String accountId, final Interval interval, @Nullable final String ticker)
            throws IOException {
        return tinkoffService.getOperations(accountId, interval, ticker);
    }

    public List<PortfolioPosition> getPortfolioPositions(final String accountId) {
        return tinkoffService.getPortfolioPositions(accountId);
    }

    public OffsetDateTime nextMinute() {
        return getFakeTinkoffService().nextMinute();
    }

    public void addInvestment(final String accountId, final OffsetDateTime dateTime, final Currency currency, final BigDecimal increment) {
        getFakeTinkoffService().addInvestment(accountId, dateTime, currency, increment);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final Currency currency) {
        return getFakeTinkoffService().getInvestments(accountId, currency);
    }

    public BigDecimal getCurrentBalance(final String accountId, final Currency currency) {
        return getFakeTinkoffService().getCurrentBalance(accountId, currency);
    }

    public BigDecimal getCurrentPrice(final String ticker) throws IOException {
        return getFakeTinkoffService().getCurrentPrice(ticker);
    }

    // endregion

}