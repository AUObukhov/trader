package ru.obukhov.trader.web.client.service.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.market.model.Operation;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

public interface OperationsClient {

    List<Operation> getOperations(
            @Nullable final String brokerAccountId,
            @NotNull final OffsetDateTime from,
            @NotNull final OffsetDateTime to,
            @Nullable final String figi
    ) throws IOException;

}