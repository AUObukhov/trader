package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.TimestampUtils;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Candle {

    protected BigDecimal open;
    protected BigDecimal close;
    protected BigDecimal high;
    protected BigDecimal low;

    protected Timestamp time;

    @JsonProperty("time")
    public String getOffsetDateTimeStringTime() {
        return TimestampUtils.toOffsetDateTimeString(time);
    }

    @JsonProperty("time")
    public void setOffsetDateTimeStringTime(final String time) {
        this.time = TimestampUtils.newTimestamp(time);
    }

    /**
     * @return candle, interpolated between given {@code leftCandle} and {@code rightCandle}
     */
    public static Candle createAverage(final Candle leftCandle, final Candle rightCandle) {
        Assert.isTrue(!TimestampUtils.isAfter(leftCandle.getTime(), rightCandle.getTime()), "leftCandle can't be after rightCandle");

        final BigDecimal open = leftCandle.getOpen();
        final BigDecimal close = rightCandle.getClose();
        final BigDecimal high = open.max(close);
        final BigDecimal low = open.min(close);
        final Timestamp time = TimestampUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(open, close, high, low, time);
    }

}