package ru.obukhov.investor.service.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.time.OffsetDateTime;
import java.util.List;

public class OperationsServiceImpl implements OperationsService {

    private final TinkoffService tinkoffService;

    public OperationsServiceImpl(TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    @Override
    public List<Operation> getOperations(@NotNull OffsetDateTime from,
                                         @NotNull OffsetDateTime to,
                                         @Nullable String ticker) {
        return tinkoffService.getOperations(from, to, ticker);
    }

}