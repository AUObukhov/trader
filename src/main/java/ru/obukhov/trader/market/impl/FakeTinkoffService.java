package ru.obukhov.trader.market.impl;

import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.config.properties.MarketProperties;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.FakeContext;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.SortedMap;

/**
 * Prices are loaded from real market, but any operations do not affect the real portfolio - all data is stored in
 * memory.
 */
@Slf4j
public class FakeTinkoffService implements TinkoffService {

    private final MarketProperties marketProperties;
    private final ExtMarketDataService extMarketDataService;

    private final FakeContext fakeContext;

    public FakeTinkoffService(final MarketProperties marketProperties, final TinkoffServices tinkoffServices, final FakeContext fakeContext) {
        this.marketProperties = marketProperties;
        this.extMarketDataService = tinkoffServices.extMarketDataService();
        this.fakeContext = fakeContext;
    }

    /**
     * Changes currentDateTime to the nearest work time after it
     *
     * @return new value of currentDateTime
     */
    public OffsetDateTime nextMinute() {
        final OffsetDateTime nextWorkMinute = DateUtils.getNextWorkMinute(
                fakeContext.getCurrentDateTime(),
                marketProperties.getWorkSchedule()
        );
        fakeContext.setCurrentDateTime(nextWorkMinute);

        return nextWorkMinute;
    }

    // region methods for back test

    @Override
    public OffsetDateTime getCurrentDateTime() {
        return fakeContext.getCurrentDateTime();
    }

    public BigDecimal getCurrentBalance(final String accountId, final Currency currency) {
        return fakeContext.getBalance(accountId, currency);
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final Currency currency) {
        return fakeContext.getInvestments(accountId, currency);
    }

    public void addInvestment(
            final String accountId,
            final OffsetDateTime dateTime,
            final Currency currency,
            final BigDecimal increment
    ) {
        fakeContext.addInvestment(accountId, dateTime, currency, increment);
    }

    /**
     * @return last known price for instrument with given {@code ticker} not after current fake date time
     */
    public BigDecimal getCurrentPrice(final String ticker) {
        return extMarketDataService.getLastCandle(ticker, fakeContext.getCurrentDateTime()).getClosePrice();
    }

    // endregion
}