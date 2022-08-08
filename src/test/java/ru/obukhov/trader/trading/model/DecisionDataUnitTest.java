package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DecisionDataUnitTest {

    @Test
    void getQuantityLots() {
        final int quantityLots = 30;

        final DecisionData decisionData = new DecisionData().setPosition(TestData.createPortfolioPosition(quantityLots));

        AssertUtils.assertEquals(quantityLots, decisionData.getQuantityLots());

    }

    // region getCurrentPrice tests

    @Test
    void getCurrentPrice_returnsNull_whenCandlesAreNull() {
        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(null);

        Assertions.assertNull(decisionData.getCurrentPrice());
    }

    @Test
    void getCurrentPrice_returnsNull_whenCandlesAreEmpty() {
        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(Collections.emptyList());

        Assertions.assertNull(decisionData.getCurrentPrice());
    }

    @Test
    void getCurrentPrice_returnsLastCandleOpenPrice() {
        final List<Candle> candles = new ArrayList<>();
        candles.add(TestData.createCandleWithOpenPrice(100));
        candles.add(TestData.createCandleWithOpenPrice(200));
        final BigDecimal lastCandleOpenPrice = BigDecimal.valueOf(300);
        candles.add(new Candle().setOpenPrice(lastCandleOpenPrice));

        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(candles);

        AssertUtils.assertEquals(lastCandleOpenPrice, decisionData.getCurrentPrice());
    }

    // endregion

}