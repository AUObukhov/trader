package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.OperationsService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.util.List;

public class OperationsServiceImpl implements OperationsService {

    private final TinkoffService tinkoffService;

    public OperationsServiceImpl(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    @Override
    public List<Operation> getOperations(@Nullable final String brokerAccountId, @NotNull final Interval interval, @Nullable final String ticker) {
        return tinkoffService.getOperations(brokerAccountId, interval, ticker);
    }

}