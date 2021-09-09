package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.util.List;

public interface OperationsService {

    List<Operation> getOperations(@Nullable final String brokerAccountId, @NotNull final Interval interval, @Nullable final String ticker);

}