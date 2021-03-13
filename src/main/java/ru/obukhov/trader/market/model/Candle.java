package ru.obukhov.trader.market.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DateUtils;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class Candle {

    private BigDecimal openPrice;

    private BigDecimal closePrice;

    private BigDecimal highestPrice;

    private BigDecimal lowestPrice;

    private OffsetDateTime time;

    private CandleInterval interval;

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

        return builder()
                .openPrice(openPrice)
                .closePrice(closePrice)
                .highestPrice(highestPrice)
                .lowestPrice(lowestPrice)
                .time(time)
                .interval(leftCandle.getInterval())
                .build();
    }

}