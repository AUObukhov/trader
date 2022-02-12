package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.OperationTypeWithCommission;

/**
 * Maps {@link OperationType} to {@link OperationTypeWithCommission}
 */
@Mapper
public interface OperationTypeMapper {

    default OperationTypeWithCommission map(final OperationType source) {
        return source == OperationType.BUY
                ? OperationTypeWithCommission.BUY
                : OperationTypeWithCommission.SELL;
    }

    default OperationType map(final OperationTypeWithCommission source) {
        return switch (source) {
            case BUY -> OperationType.BUY;
            case SELL -> OperationType.SELL;
            default -> throw new IllegalArgumentException("Expected buy or sell operation, got " + source);
        };
    }

}