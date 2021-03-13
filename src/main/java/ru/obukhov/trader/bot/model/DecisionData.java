package ru.obukhov.trader.bot.model;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.operations.Operation;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DecisionData {

    private BigDecimal balance;
    private PortfolioPosition position;
    private List<Candle> currentCandles;
    private List<Operation> lastOperations;
    private Instrument instrument;

    public Integer getPositionLotsCount() {
        return position.getLotsCount();
    }

    public BigDecimal getAveragePositionPrice() {
        return position.getAveragePositionPrice();
    }

    public BigDecimal getCurrentPrice() {
        return CollectionUtils.isEmpty(currentCandles)
                ? null
                : currentCandles.get(currentCandles.size() - 1).getClosePrice();
    }

    public int getLotSize() {
        return instrument.lot;
    }

}