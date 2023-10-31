package ru.obukhov.trader.trading.model;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.obukhov.trader.market.model.Share;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class DecisionData {

    private BigDecimal balance;
    private Position position;
    private List<Operation> lastOperations;
    private Share share;
    private BigDecimal commission;

    public Long getQuantity() {
        return position.getQuantity().longValueExact();
    }

    public BigDecimal getAveragePositionPrice() {
        return position.getAveragePositionPrice().getValue();
    }

}