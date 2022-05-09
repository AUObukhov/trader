package ru.obukhov.trader.trading.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Share;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class DecisionData {

    private BigDecimal balance;
    private PortfolioPosition position;
    private List<Candle> currentCandles;
    private List<Operation> lastOperations;
    private Share share;
    private double commission;

    public long getPositionLotsCount() {
        return position.count() / share.getLot();
    }

    public BigDecimal getAveragePositionPrice() {
        return position.averagePositionPrice().value();
    }

    public BigDecimal getCurrentPrice() {
        return CollectionUtils.isEmpty(currentCandles)
                ? null
                : currentCandles.get(currentCandles.size() - 1).getOpenPrice();
    }

    public int getLotSize() {
        return share.getLot();
    }

}