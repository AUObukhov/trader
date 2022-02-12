package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Candle {

    @JsonAlias("o")
    protected BigDecimal openPrice;

    @JsonAlias("c")
    protected BigDecimal closePrice;

    @JsonAlias("h")
    protected BigDecimal highestPrice;

    @JsonAlias("l")
    protected BigDecimal lowestPrice;

    protected OffsetDateTime time;

    protected CandleResolution interval;

    /**
     * @return candle, interpolated between given {@code leftCandle} and {@code rightCandle}
     */
    public static Candle createAverage(final Candle leftCandle, final Candle rightCandle) {
        Assert.isTrue(!leftCandle.getTime().isAfter(rightCandle.getTime()), "leftCandle can't be after rightCandle");
        Assert.isTrue(leftCandle.getInterval() == rightCandle.getInterval(), "Candle intervals must be equal");

        final BigDecimal openPrice = leftCandle.getOpenPrice();
        final BigDecimal closePrice = rightCandle.getClosePrice();
        final BigDecimal highestPrice = openPrice.max(closePrice);
        final BigDecimal lowestPrice = openPrice.min(closePrice);
        final OffsetDateTime time = DateUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(openPrice, closePrice, highestPrice, lowestPrice, time, leftCandle.getInterval());
    }

}