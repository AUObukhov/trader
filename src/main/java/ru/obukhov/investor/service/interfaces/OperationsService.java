package ru.obukhov.investor.service.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.time.OffsetDateTime;
import java.util.List;

public interface OperationsService {

    List<Operation> getOperations(@NotNull OffsetDateTime from,
                                  @NotNull OffsetDateTime to,
                                  @Nullable String ticker,
                                  @Nullable String brokerAccountId);

}