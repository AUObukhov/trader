package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.OperationMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public class FakeExtOperationsService implements ExtOperationsService {

    private static final OperationMapper OPERATION_MAPPER = Mappers.getMapper(OperationMapper.class);

    private final FakeContext fakeContext;

    @Override
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String ticker) {
        Stream<BackTestOperation> operationsStream = fakeContext.getOperations(accountId).stream()
                .filter(operation -> interval.contains(operation.dateTime()));
        if (ticker != null) {
            operationsStream = operationsStream.filter(operation -> ticker.equals(operation.ticker()));
        }

        return operationsStream
                .sorted(Comparator.comparing(operation -> operation.dateTime().toInstant()))
                .map(OPERATION_MAPPER::map)
                .toList();
    }

    @Override
    public List<PortfolioPosition> getPositions(final String accountId) {
        return fakeContext.getPositions(accountId);
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        final List<MoneyValue> money = fakeContext.getBalances(accountId).entrySet().stream()
                .map(entry -> DataStructsHelper.createMoneyValue(entry.getKey(), entry.getValue()))
                .toList();
        return DataStructsHelper.createWithdrawLimits(money);
    }

}