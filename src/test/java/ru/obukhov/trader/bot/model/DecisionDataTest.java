package ru.obukhov.trader.bot.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DecisionDataTest {

    @Test
    void getCurrentPrice_returnsNull_whenCandlesAreNull() {
        DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(null);

        Assertions.assertNull(decisionData.getCurrentPrice());
    }

    @Test
    void getCurrentPrice_returnsNull_whenCandlesAreEmpty() {
        DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(Collections.emptyList());

        Assertions.assertNull(decisionData.getCurrentPrice());
    }

    @Test
    void getCurrentPrice_returnsLastCandleOpenPrice() {
        List<Candle> candles = new ArrayList<>();
        Candle.CandleBuilder candleBuilder = Candle.builder();
        candles.add(candleBuilder.openPrice(BigDecimal.valueOf(100)).build());
        candles.add(candleBuilder.openPrice(BigDecimal.valueOf(200)).build());
        BigDecimal lastCandleOpenPrice = BigDecimal.valueOf(300);
        candles.add(candleBuilder.openPrice(lastCandleOpenPrice).build());

        DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(candles);

        AssertUtils.assertEquals(lastCandleOpenPrice, decisionData.getCurrentPrice());
    }

}