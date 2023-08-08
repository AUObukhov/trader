package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.piapi.contract.v1.Operation;

/**
 * Maps {@link Operation} to {@link BackTestOperation} and vice versa
 */
@Mapper(uses = {DateTimeMapper.class, MoneyMapper.class})
public interface OperationMapper {

    @Mapping(target = "timestamp", source = "operation.date")
    @Mapping(target = "figi", source = "figi")
    BackTestOperation map(final String figi, final Operation operation);

    @Mapping(target = "date", source = "timestamp")
    @Mapping(target = "quantity", source = "quantity")
    Operation map(final BackTestOperation source);

}