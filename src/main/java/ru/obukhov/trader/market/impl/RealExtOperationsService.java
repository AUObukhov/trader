package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
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
    private final RealExtInstrumentsService realExtInstrumentsService;

    @Override
    public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @NotNull final String figi) {
        return operationsService.getAllOperationsSync(accountId, interval.getFrom().toInstant(), interval.getTo().toInstant(), figi);
    }

    @Override
    public List<PortfolioPosition> getPositions(final String accountId) {
        return operationsService.getPortfolioSync(accountId).getPositions().stream()
                .map(position -> {
                    final String ticker = realExtInstrumentsService.getTickerByFigi(position.getFigi());
                    return POSITION_MAPPER.map(ticker, position);
                })
                .toList();
    }

    @Override
    public WithdrawLimits getWithdrawLimits(final String accountId) {
        return operationsService.getWithdrawLimitsSync(accountId);
    }


}