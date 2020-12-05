package ru.obukhov.investor.model.transform;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.operations.Operation;

/**
 * Maps {@link Operation} to {@link SimulatedOperation}
 */
@Mapper
public abstract class OperationMapper {

    @Mapping(target = "dateTime", source = "date")
    @Mapping(target = "commission", source = "commission.value")
    public abstract SimulatedOperation map(Operation source);

    @AfterMapping
    protected void calculateAmount(@MappingTarget SimulatedOperation target, Operation source) {
        if (source.price != null && source.quantity != null) {
            target.setAmount(MathUtils.multiply(source.price, source.quantity));
        }
    }

}