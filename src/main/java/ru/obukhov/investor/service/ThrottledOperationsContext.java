package ru.obukhov.investor.service;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import ru.obukhov.investor.service.aop.Throttled;
import ru.tinkoff.invest.openapi.OperationsContext;
import ru.tinkoff.invest.openapi.models.operations.OperationsList;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;

@Component
public class ThrottledOperationsContext implements OperationsContext {

    @Setter
    private OperationsContext innerContext;

    @Throttled
    @NotNull
    @Override
    public CompletableFuture<OperationsList> getOperations(@NotNull OffsetDateTime from,
                                                           @NotNull OffsetDateTime to,
                                                           @Nullable String figi,
                                                           @Nullable String brokerAccountId) {
        return innerContext.getOperations(from, to, figi, brokerAccountId);
    }

    @NotNull
    @Override
    public String getPath() {
        return innerContext.getPath();
    }
}