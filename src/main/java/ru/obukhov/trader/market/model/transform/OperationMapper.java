package ru.obukhov.trader.market.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;

import java.math.BigDecimal;

/**
 * Maps {@link Operation} to {@link SimulatedOperation}
 */
@Mapper
public interface OperationMapper {

    @Mapping(target = "dateTime", source = "date")
    @Mapping(target = "commission", source = "commission.value")
    SimulatedOperation map(Operation source);

    default Operation map(SimulatedOperation source) {
        BigDecimal totalPrice = MathUtils.multiply(source.getPrice(), source.getQuantity());
        MoneyAmount commission = new MoneyAmount(Currency.RUB, source.getCommission());
        return new Operation(StringUtils.EMPTY,
                OperationStatus.Done,
                null,
                commission,
                Currency.RUB,
                totalPrice,
                source.getPrice(),
                source.getQuantity(),
                StringUtils.EMPTY,
                InstrumentType.Stock,
                false,
                source.getDateTime(),
                source.getOperationType());
    }

}