package ru.obukhov.trader.market.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Candle {

    protected Quotation open;
    protected Quotation close;
    protected Quotation high;
    protected Quotation low;

    protected OffsetDateTime time;

    /**
     * @return candle, interpolated between given {@code leftCandle} and {@code rightCandle}
     */
    public static Candle createAverage(final Candle leftCandle, final Candle rightCandle) {
        Assert.isTrue(!leftCandle.getTime().isAfter(rightCandle.getTime()), "leftCandle can't be after rightCandle");

        final Quotation open = leftCandle.getOpen();
        final Quotation close = rightCandle.getClose();
        final Quotation high = QuotationUtils.max(open, close);
        final Quotation low = QuotationUtils.min(open, close);
        final OffsetDateTime time = DateUtils.getAverage(leftCandle.getTime(), rightCandle.getTime());

        return new Candle(open, close, high, low, time);
    }

}