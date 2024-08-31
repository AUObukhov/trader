package ru.obukhov.trader.market.model;

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

    protected BigDecimal open;
    protected BigDecimal close;
    protected BigDecimal high;
    protected BigDecimal low;

    protected OffsetDateTime time;

    public static Candle createAverage(final Candle leftCandle, final Candle rightCandle) {
        Assert.isTrue(!leftCandle.getTime().isAfter(rightCandle.getTime()), "leftCandle can't be after rightCandle");

        final BigDecimal open = leftCandle.getOpen();
        final BigDecimal close = rightCandle.getClose();
        final BigDecimal high = open.max(close);
        final BigDecimal low = open.min(close);
        final OffsetDateTime time = DateUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(open, close, high, low, time);
    }

}