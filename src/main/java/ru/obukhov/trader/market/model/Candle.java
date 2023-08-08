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

    protected BigDecimal openPrice;
    protected BigDecimal closePrice;
    protected BigDecimal highestPrice;
    protected BigDecimal lowestPrice;

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

        final BigDecimal openPrice = leftCandle.getOpenPrice();
        final BigDecimal closePrice = rightCandle.getClosePrice();
        final BigDecimal highestPrice = openPrice.max(closePrice);
        final BigDecimal lowestPrice = openPrice.min(closePrice);
        final Timestamp time = TimestampUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(openPrice, closePrice, highestPrice, lowestPrice, time);
    }

}