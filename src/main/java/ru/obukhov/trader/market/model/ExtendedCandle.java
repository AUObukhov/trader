package ru.obukhov.trader.market.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Class of Candle with additional statistic fields
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExtendedCandle extends Candle {

    private BigDecimal averagePrice;

    private Extremum extremum;

    public ExtendedCandle(Candle candle, BigDecimal averagePrice) {
        super(
                candle.getOpenPrice(),
                candle.getClosePrice(),
                candle.getHighestPrice(),
                candle.getLowestPrice(),
                candle.getTime(),
                candle.getInterval()
        );
        this.averagePrice = averagePrice;
        this.extremum = Extremum.NONE;
    }

    public boolean isLocalMaximum() {
        return extremum == Extremum.MAX;
    }

    public boolean isLocalMinimum() {
        return extremum == Extremum.MIN;
    }

}