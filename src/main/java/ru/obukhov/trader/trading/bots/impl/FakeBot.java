package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketInstrumentsService;
import ru.obukhov.trader.market.impl.MarketOperationsService;
import ru.obukhov.trader.market.impl.MarketOrdersService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.impl.PortfolioService;
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
            final MarketService marketService,
            final MarketInstrumentsService marketInstrumentsService,
            final MarketOperationsService operationsService,
            final MarketOrdersService ordersService,
            final PortfolioService portfolioService,
            final FakeTinkoffService fakeTinkoffService,
            final TradingStrategy strategy
    ) {
        super(
                marketService,
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

    public List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, @Nullable final String ticker)
            throws IOException {
        return tinkoffService.getOperations(brokerAccountId, interval, ticker);
    }

    public List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) throws IOException {
        return tinkoffService.getPortfolioPositions(brokerAccountId);
    }

    public OffsetDateTime nextMinute() {
        return getFakeTinkoffService().nextMinute();
    }

    public void addInvestment(final String brokerAccountId, final OffsetDateTime dateTime, final String currency, final BigDecimal increment) {
        getFakeTinkoffService().addInvestment(brokerAccountId, dateTime, currency, increment);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(@Nullable final String brokerAccountId, final String currency) {
        return getFakeTinkoffService().getInvestments(brokerAccountId, currency);
    }

    public BigDecimal getCurrentBalance(@Nullable final String brokerAccountId, final String currency) {
        return getFakeTinkoffService().getCurrentBalance(brokerAccountId, currency);
    }

    public BigDecimal getCurrentPrice(final String ticker) throws IOException {
        return getFakeTinkoffService().getCurrentPrice(ticker);
    }

    // endregion

}