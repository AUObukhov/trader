package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.Operation;

/**
 * Maps {@link Operation} to {@link SimulatedOperation} and vice versa
 */
@Mapper(uses = OperationTypeMapper.class)
public interface OperationMapper {

    @Mapping(target = "dateTime", source = "date")
    @Mapping(target = "commission", source = "commission.value")
    SimulatedOperation map(Operation source);

    @Mapping(target = "date", source = "dateTime")
    @Mapping(target = "commission.value", source = "commission")
    Operation map(SimulatedOperation source);

}