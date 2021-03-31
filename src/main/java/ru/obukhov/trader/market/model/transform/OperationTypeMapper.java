package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

/**
 * Maps {@link OperationType} to {@link OperationTypeWithCommission}
 */
@Mapper
public interface OperationTypeMapper {

    default OperationTypeWithCommission map(OperationType source) {
        return source == OperationType.BUY
                ? OperationTypeWithCommission.BUY
                : OperationTypeWithCommission.SELL;
    }

    default OperationType map(OperationTypeWithCommission source) {
        switch (source) {
            case BUY:
                return OperationType.BUY;
            case SELL:
                return OperationType.SELL;
            default:
                throw new IllegalArgumentException("Expected buy or sell operation, got " + source);
        }
    }

}