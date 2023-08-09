package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
public class RealExtOperationsService implements ExtOperationsService {

    private final OperationsService operationsService;

    @Override
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @NotNull final String figi) {
        final Instant from = TimestampUtils.toInstant(interval.getFrom());
        final Instant to = TimestampUtils.toInstant(interval.getTo());
        return operationsService.getAllOperationsSync(accountId, from, to, figi);
    }

    @Override
    public List<Position> getPositions(final String accountId) {
        return operationsService.getPortfolioSync(accountId).getPositions();
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        return operationsService.getWithdrawLimitsSync(accountId);
    }


}