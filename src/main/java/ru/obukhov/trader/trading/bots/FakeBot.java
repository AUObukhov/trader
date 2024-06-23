package ru.obukhov.trader.trading.bots;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.impl.FakeContext;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.interfaces.ExtOrdersService;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class FakeBot extends Bot {

    public FakeBot(
            final ExtMarketDataService extMarketDataService,
            final ExtInstrumentsService extInstrumentsService,
            final ExtOperationsService extOperationsService,
            final ExtOrdersService extOrdersService,
            final ExtUsersService extUsersService,
            final FakeContext fakeContext,
            final TradingStrategy strategy
    ) {
        super(
                extMarketDataService,
                extInstrumentsService,
                extOperationsService,
                extOrdersService,
                extUsersService,
                fakeContext,
                strategy
        );
    }

    public Map<String, List<Operation>> getOperations(final String accountId, final Interval interval, final List<String> figies) {
        return figies.stream()
                .collect(Collectors.toMap(Function.identity(), figi -> extOperationsService.getOperations(accountId, interval, figi)));
    }

    public List<Position> getPortfolioPositions(final String accountId) {
        return extOperationsService.getPositions(accountId);
    }

    // region FakeContext proxy

    public OffsetDateTime getCurrentDateTime() {
        return context.getCurrentDateTime();
    }

    public OffsetDateTime nextScheduleMinute(final List<TradingDay> tradingSchedule) {
        return getFakeContext().nextScheduleMinute(tradingSchedule);
    }

    public void addInvestments(final String accountId, final OffsetDateTime dateTime, final Map<String, BigDecimal> investments) {
        getFakeContext().addInvestments(accountId, dateTime, investments);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final String currency) {
        return getFakeContext().getInvestments(accountId, currency);
    }

    public BigDecimal getCurrentBalance(final String accountId, final String currency) {
        return getFakeContext().getBalance(accountId, currency);
    }

    public BigDecimal getCurrentPrice(final String figi, final OffsetDateTime dateTime) {
        final OffsetDateTime innerDateTime = ObjectUtils.defaultIfNull(context.getCurrentDateTime(), dateTime);
        return extMarketDataService.getPrice(figi, innerDateTime);
    }

    private FakeContext getFakeContext() {
        return (FakeContext) context;
    }

    // endregion

}