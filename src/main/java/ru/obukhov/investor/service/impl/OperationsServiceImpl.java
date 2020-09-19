package ru.obukhov.investor.service.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.MarketService;
import ru.obukhov.investor.service.OperationsService;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OperationsServiceImpl implements OperationsService {

    private final MarketService marketService;
    private final OperationsContext operationsContext;

    @Override
    public List<Operation> getOperations(@NotNull OffsetDateTime from,
                                         @NotNull OffsetDateTime to,
                                         @Nullable String ticker,
                                         @Nullable String brokerAccountId) {
        String figi = marketService.getFigi(ticker);
        return operationsContext.getOperations(from, to, figi, brokerAccountId).join().operations;
    }

}