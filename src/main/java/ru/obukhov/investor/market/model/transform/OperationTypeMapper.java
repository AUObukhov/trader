package ru.obukhov.investor.market.model.transform;

import org.mapstruct.Mapper;
import ru.tinkoff.invest.openapi.models.operations.OperationType;
import ru.tinkoff.invest.openapi.models.orders.Operation;

/**
 * Maps {@link Operation} to {@link OperationType}
 */
@Mapper
public interface OperationTypeMapper {

    default OperationType map(Operation source) {
        return source == ru.tinkoff.invest.openapi.models.orders.Operation.Buy
                ? OperationType.Buy
                : OperationType.Sell;
    }

}