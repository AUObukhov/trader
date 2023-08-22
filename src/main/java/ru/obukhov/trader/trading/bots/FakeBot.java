package ru.obukhov.trader.trading.bots;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.core.models.Position;

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

    public Share getShare(final String figi) {
        return extInstrumentsService.getShare(figi);
    }

    public List<Operation> getOperations(final String accountId, final Interval interval, final String figi) {
        return extOperationsService.getOperations(accountId, interval, figi);
    }

    public List<Position> getPortfolioPositions(final String accountId) {
        return extOperationsService.getPositions(accountId);
    }

    // region FakeContext proxy

    public Timestamp getCurrentTimestamp() {
        return context.getCurrentTimestamp();
    }

    public Timestamp nextScheduleMinute(final List<TradingDay> tradingSchedule) {
        return getFakeContext().nextScheduleMinute(tradingSchedule);
    }

    public void addInvestment(final String accountId, final Timestamp timestamp, final String currency, final Quotation increment) {
        getFakeContext().addInvestment(accountId, timestamp, currency, increment);
    }

    public SortedMap<Timestamp, Quotation> getInvestments(final String accountId, final String currency) {
        return getFakeContext().getInvestments(accountId, currency);
    }

    public Quotation getCurrentBalance(final String accountId, final String currency) {
        return getFakeContext().getBalance(accountId, currency);
    }

    public Quotation getCurrentPrice(final String figi) {
        return extMarketDataService.getLastPrice(figi, context.getCurrentTimestamp());
    }

    private FakeContext getFakeContext() {
        return (FakeContext) context;
    }

    // endregion

}