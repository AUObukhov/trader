package ru.obukhov.trader.trading.bots;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.SortedMap;

@Slf4j
public class FakeBot extends Bot {

    public FakeBot(
            final ExtMarketDataService extMarketDataService,
            final ExtInstrumentsService extInstrumentsService,
            final ExtOperationsService extOperationsService,
            final ExtOrdersService ordersService,
            final FakeContext fakeContext,
            final TradingStrategy strategy
    ) {
        super(
                extMarketDataService,
                extInstrumentsService,
                extOperationsService,
                ordersService,
                fakeContext,
                strategy,
                strategy.initCache()
        );
    }

    public Share getShare(final String ticker) {
        return extInstrumentsService.getSingleShare(ticker);
    }

    public List<Operation> getOperations(final String accountId, final Interval interval, @Nullable final String ticker) {
        return extOperationsService.getOperations(accountId, interval, ticker);
    }

    public List<PortfolioPosition> getPortfolioPositions(final String accountId) {
        return extOperationsService.getPositions(accountId);
    }

    // region FakeContext proxy

    public OffsetDateTime getCurrentDateTime() {
        return context.getCurrentDateTime();
    }

    public OffsetDateTime nextMinute() {
        return getFakeContext().nextMinute();
    }

    public void addInvestment(final String accountId, final OffsetDateTime dateTime, final Currency currency, final BigDecimal increment) {
        getFakeContext().addInvestment(accountId, dateTime, currency, increment);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final Currency currency) {
        return getFakeContext().getInvestments(accountId, currency);
    }

    public BigDecimal getCurrentBalance(final String accountId, final Currency currency) {
        return getFakeContext().getBalance(accountId, currency);
    }

    public BigDecimal getCurrentPrice(final String ticker) {
        return extMarketDataService.getLastPrice(ticker, context.getCurrentDateTime());
    }

    private FakeContext getFakeContext() {
        return (FakeContext) context;
    }

    // endregion

}