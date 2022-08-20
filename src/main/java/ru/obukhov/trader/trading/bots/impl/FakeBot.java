package ru.obukhov.trader.trading.bots.impl;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtOrdersService;
import ru.obukhov.trader.market.impl.FakeTinkoffService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
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
            final ExtInstrumentsService extInstrumentsService,
            final ExtOperationsService extOperationsService,
            final ExtOrdersService ordersService,
            final FakeTinkoffService fakeTinkoffService,
            final TradingStrategy strategy
    ) {
        super(
                extMarketDataService,
                extInstrumentsService,
                extOperationsService,
                ordersService,
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
        return extInstrumentsService.getShare(ticker);
    }

    public OffsetDateTime getCurrentDateTime() {
        return tinkoffService.getCurrentDateTime();
    }

    public List<Operation> getOperations(final String accountId, final Interval interval, @Nullable final String ticker)
            throws IOException {
        return extOperationsService.getOperations(accountId, interval, ticker);
    }

    public List<PortfolioPosition> getPortfolioPositions(final String accountId) {
        return extOperationsService.getPositions(accountId);
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