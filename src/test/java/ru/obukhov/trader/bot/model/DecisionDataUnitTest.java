package ru.obukhov.trader.bot.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

class DecisionDataUnitTest {

    @Test
    void getCurrentPrice_returnsNull_whenCandlesAreNull() {
        DecisionData decisionData = new DecisionData()
                .withCurrentCandles(null);

        Assertions.assertNull(decisionData.getCurrentPrice());
    }

    @Test
    void getCurrentPrice_returnsNull_whenCandlesAreEmpty() {
        DecisionData decisionData = new DecisionData()
                .withCurrentCandles(Collections.emptyList());

        Assertions.assertNull(decisionData.getCurrentPrice());
    }

    @Test
    void getCurrentPrice_returnsLastCandleOpenPrice() {
        Candle candle1 = TestDataHelper.createCandleWithOpenPrice(100);
        Candle candle2 = TestDataHelper.createCandleWithOpenPrice(200);
        Candle candle3 = TestDataHelper.createCandleWithOpenPrice(BigDecimal.valueOf(300));

        DecisionData decisionData = new DecisionData()
                .withCurrentCandles(Arrays.asList(candle1, candle2, candle3));

        AssertUtils.assertEquals(candle3.getOpenPrice(), decisionData.getCurrentPrice());
    }

}