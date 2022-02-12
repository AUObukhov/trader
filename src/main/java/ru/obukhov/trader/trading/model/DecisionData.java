package ru.obukhov.trader.trading.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Operation;
import ru.obukhov.trader.market.model.PortfolioPosition;

import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class DecisionData {

    private BigDecimal balance;
    private PortfolioPosition position;
    private List<Candle> currentCandles;
    private List<Operation> lastOperations;
    private MarketInstrument instrument;
    private double commission;

    public Integer getPositionLotsCount() {
        return position.getCount() / instrument.getLot();
    }

    public BigDecimal getAveragePositionPrice() {
        return position.getAveragePositionPrice();
    }

    public BigDecimal getCurrentPrice() {
        return CollectionUtils.isEmpty(currentCandles)
                ? null
                : currentCandles.get(currentCandles.size() - 1).getOpenPrice();
    }

    public int getLotSize() {
        return instrument.getLot();
    }

}