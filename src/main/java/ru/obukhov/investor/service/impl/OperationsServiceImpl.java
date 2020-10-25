package ru.obukhov.investor.service.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.service.TinkoffContextsAware;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OperationsService;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class OperationsServiceImpl extends TinkoffContextsAware implements OperationsService {

    private final MarketService marketService;

    public OperationsServiceImpl(ConnectionService connectionService, MarketService marketService) {
        super(connectionService);
        this.marketService = marketService;
    }

    @Override
    public List<Operation> getOperations(@NotNull OffsetDateTime from,
                                         @NotNull OffsetDateTime to,
                                         @Nullable String ticker) {
        return getOperations(from, to, ticker, null);
    }

    @Override
    public List<Operation> getOperations(@NotNull OffsetDateTime from,
                                         @NotNull OffsetDateTime to,
                                         @Nullable String ticker,
                                         @Nullable String brokerAccountId) {
        String figi = ticker == null ? null : marketService.getFigi(ticker);
        return getOperationsContext().getOperations(from, to, figi, brokerAccountId).join().operations;
    }

}