package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.util.List;

/**
 * Service to get info about customer operations at market
 */
public class OperationsService {

    private final TinkoffService tinkoffService;

    public OperationsService(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    /**
     * @return list of operations with given {@code ticker} at given {@code brokerAccountId} made in given {@code interval}.
     * If {@code brokerAccountId} null, works with default broker account
     */
    public List<Operation> getOperations(@Nullable final String brokerAccountId, @NotNull final Interval interval, @Nullable final String ticker) {
        return tinkoffService.getOperations(brokerAccountId, interval, ticker);
    }

}