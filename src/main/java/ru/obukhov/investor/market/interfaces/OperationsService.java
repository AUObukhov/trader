package ru.obukhov.investor.market.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.investor.common.model.Interval;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.util.List;

public interface OperationsService {

    List<Operation> getOperations(@NotNull Interval interval, @Nullable String ticker);

}