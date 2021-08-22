package ru.obukhov.trader.trading.model;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.Operation;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DecisionData {

    private BigDecimal balance;
    private PortfolioPosition position;
    private List<Candle> currentCandles;
    private List<Operation> lastOperations;
    private MarketInstrument instrument;

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