package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.trading.bots.interfaces.Bot;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.SortedMap;

@Slf4j
public class FakeBot extends AbstractBot implements Bot {

    public FakeBot(
            final MarketService marketService,
            final OperationsService operationsService,
            final OrdersService ordersService,
            final PortfolioService portfolioService,
            final FakeTinkoffService fakeTinkoffService,
            final TradingStrategy strategy
    ) {
        super(marketService, operationsService, ordersService, portfolioService, fakeTinkoffService, strategy, strategy.initCache());
    }

    public FakeTinkoffService getFakeTinkoffService() {
        return (FakeTinkoffService) tinkoffService;
    }

    // region FakeTinkoffService proxy

    public MarketInstrument searchMarketInstrument(final String ticker) {
        return tinkoffService.searchMarketInstrument(ticker);
    }

    public OffsetDateTime getCurrentDateTime() {
        return tinkoffService.getCurrentDateTime();
    }

    public List<Operation> getOperations(@Nullable final String brokerAccountId, final Interval interval, @Nullable final String ticker) {
        return tinkoffService.getOperations(brokerAccountId, interval, ticker);
    }

    public List<PortfolioPosition> getPortfolioPositions(@Nullable final String brokerAccountId) {
        return tinkoffService.getPortfolioPositions(brokerAccountId);
    }

    public OffsetDateTime nextMinute() {
        return getFakeTinkoffService().nextMinute();
    }

    public void addInvestment(final String brokerAccountId, final OffsetDateTime dateTime, final Currency currency, final BigDecimal increment) {
        getFakeTinkoffService().addInvestment(brokerAccountId, dateTime, currency, increment);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(@Nullable final String brokerAccountId, final Currency currency) {
        return getFakeTinkoffService().getInvestments(brokerAccountId, currency);
    }

    public BigDecimal getCurrentBalance(@Nullable final String brokerAccountId, final Currency currency) {
        return getFakeTinkoffService().getCurrentBalance(brokerAccountId, currency);
    }

    public BigDecimal getCurrentPrice(final String ticker) {
        return getFakeTinkoffService().getCurrentPrice(ticker);
    }

    // endregion

}