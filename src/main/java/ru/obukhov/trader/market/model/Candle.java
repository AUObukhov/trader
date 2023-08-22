package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Candle {

    protected Quotation open;
    protected Quotation close;
    protected Quotation high;
    protected Quotation low;

    protected Timestamp time;

    @JsonProperty("time")
    @SuppressWarnings("unused")
    public String getOffsetDateTimeStringTime() {
        return TimestampUtils.toOffsetDateTimeString(time);
    }

    @JsonProperty("time")
    @SuppressWarnings("unused")
    public void setOffsetDateTimeStringTime(final String time) {
        this.time = TimestampUtils.newTimestamp(time);
    }

    /**
     * @return candle, interpolated between given {@code leftCandle} and {@code rightCandle}
     */
    public static Candle createAverage(final Candle leftCandle, final Candle rightCandle) {
        Assert.isTrue(!TimestampUtils.isAfter(leftCandle.getTime(), rightCandle.getTime()), "leftCandle can't be after rightCandle");

        final Quotation open = leftCandle.getOpen();
        final Quotation close = rightCandle.getClose();
        final Quotation high = QuotationUtils.max(open, close);
        final Quotation low = QuotationUtils.min(open, close);
        final Timestamp time = TimestampUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(open, close, high, low, time);
    }

}