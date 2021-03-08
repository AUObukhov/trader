package ru.obukhov.investor.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.market.interfaces.OperationsService;
import ru.obukhov.investor.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.models.operations.Operation;

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