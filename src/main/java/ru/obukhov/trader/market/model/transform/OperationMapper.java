package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.trading.model.BackTestOperation;

/**
 * Maps {@link Operation} to {@link BackTestOperation} and vice versa
 */
@Mapper(uses = OperationTypeMapper.class)
public interface OperationMapper {

    @Mapping(target = "dateTime", source = "operation.date")
    @Mapping(target = "commission", source = "operation.commission.value")
    @Mapping(target = "ticker", source = "ticker")
    BackTestOperation map(final String ticker, final Operation operation);

    @Mapping(target = "date", source = "dateTime")
    @Mapping(target = "commission.value", source = "commission")
    @Mapping(target = "commission.currency", constant = "RUB")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "quantityExecuted", source = "quantity")
    Operation map(final BackTestOperation source);

}