package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.model.transform.PositionMapper;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.util.List;

@AllArgsConstructor
public class RealExtOperationsService implements ExtOperationsService {

    private static final PositionMapper POSITION_MAPPER = Mappers.getMapper(PositionMapper.class);

    private final OperationsService operationsService;
    private final ExtInstrumentsService extInstrumentsService;

    @Override
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String ticker) {
        final String figi = extInstrumentsService.getFigiByTicker(ticker);
        return operationsService.getAllOperationsSync(accountId, interval.getFrom().toInstant(), interval.getTo().toInstant(), figi);
    }

    @Override
    public List<PortfolioPosition> getPositions(final String accountId) {
        return operationsService.getPortfolioSync(accountId).getPositions().stream()
                .map(position -> {
                    final String ticker = extInstrumentsService.getTickerByFigi(position.getFigi());
                    return POSITION_MAPPER.map(ticker, position);
                })
                .toList();
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        return operationsService.getWithdrawLimitsSync(accountId);
    }


}