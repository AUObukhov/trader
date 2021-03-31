package ru.obukhov.trader.market.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candle {

    protected BigDecimal openPrice;

    protected BigDecimal closePrice;

    protected BigDecimal highestPrice;

    protected BigDecimal lowestPrice;

    protected OffsetDateTime time;

    protected CandleResolution interval;

    /**
     * @return candle, interpolated between given {@code leftCandle} and {@code rightCandle}
     */
    public static Candle createAverage(Candle leftCandle, Candle rightCandle) {
        Assert.isTrue(!leftCandle.getTime().isAfter(rightCandle.getTime()),
                "leftCandle can't be after rightCandle");
        Assert.isTrue(leftCandle.getInterval() == rightCandle.getInterval(),
                "Candle intervals must be equal");

        BigDecimal openPrice = leftCandle.getClosePrice();
        BigDecimal closePrice = rightCandle.getOpenPrice();
        BigDecimal highestPrice = openPrice.max(closePrice);
        BigDecimal lowestPrice = openPrice.min(closePrice);
        OffsetDateTime time = DateUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(
                openPrice,
                closePrice,
                highestPrice,
                lowestPrice,
                time,
                leftCandle.getInterval()
        );
    }

}