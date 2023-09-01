package ru.obukhov.trader.trading.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.Share;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class DecisionData {

    private Quotation balance;
    private Position position;
    private List<Candle> currentCandles;
    private List<Operation> lastOperations;
    private Share share;
    private Quotation commission;

    public Long getQuantity() {
        return position.getQuantity().longValueExact();
    }

    public BigDecimal getAveragePositionPrice() {
        return position.getAveragePositionPrice().getValue();
    }

    public Quotation getCurrentPrice() {
        return CollectionUtils.isEmpty(currentCandles)
                ? null
                : currentCandles.get(currentCandles.size() - 1).getOpen();
    }

    public int getLotSize() {
        return share.lot();
    }

}