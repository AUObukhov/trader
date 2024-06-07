package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class RealExtOperationsService implements ExtOperationsService {

    private final OperationsService operationsService;

    @Override
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @NotNull final String figi) {
        final Instant from = interval.getFrom().toInstant();
        final Instant to = interval.getTo().toInstant();
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