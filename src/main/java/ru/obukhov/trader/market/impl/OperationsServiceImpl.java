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

    public OperationsServiceImpl(TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    @Override
    public List<Operation> getOperations(@NotNull Interval interval, @Nullable String ticker) {
        return tinkoffService.getOperations(interval, ticker);
    }

}