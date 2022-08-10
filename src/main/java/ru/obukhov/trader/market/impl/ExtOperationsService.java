package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.piapi.contract.v1.Operation;

import java.io.IOException;
import java.util.List;

/**
 * Service to get info about customer operations at market
 */
public class ExtOperationsService {

    private final TinkoffService tinkoffService;

    public ExtOperationsService(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    /**
     * @return list of operations with given {@code ticker} at given {@code accountId} made in given {@code interval}.
     * If {@code accountId} null, works with default broker account
     */
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String ticker)
            throws IOException {
        return tinkoffService.getOperations(accountId, interval, ticker);
    }

}