package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
public class FakeExtOperationsService implements ExtOperationsService {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    private final FakeContext fakeContext;

    @Override
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String figi) {
        Stream<Operation> operationsStream = fakeContext.getOperations(accountId).stream()
                .filter(operation -> interval.contains(DATE_TIME_MAPPER.timestampToOffsetDateTime(operation.getDate())));
        if (figi != null) {
            operationsStream = operationsStream.filter(operation -> figi.equals(operation.getFigi()));
        }

        return operationsStream
                .sorted(Comparator.comparing(operation -> TimestampUtils.toInstant(operation.getDate())))
                .toList();
    }

    @Override
    public List<Position> getPositions(final String accountId) {
        return fakeContext.getPositions(accountId);
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        final List<MoneyValue> money = fakeContext.getBalances(accountId).entrySet().stream()
                .map(entry -> DataStructsHelper.newMoneyValue(entry.getKey(), entry.getValue()))
                .toList();
        return DataStructsHelper.newWithdrawLimits(money);
    }

}