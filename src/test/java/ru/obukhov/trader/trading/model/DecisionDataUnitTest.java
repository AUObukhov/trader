package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.PortfolioPositionBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DecisionDataUnitTest {

    @Test
    void getQuantityLots() {
        final int quantityLots = 30;

        final PortfolioPosition portfolioPosition = new PortfolioPositionBuilder().setQuantityLots(quantityLots).build();
        final DecisionData decisionData = new DecisionData().setPosition(portfolioPosition);

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
        candles.add(new CandleBuilder().setOpenPrice(100).build());
        candles.add(new CandleBuilder().setOpenPrice(200).build());
        final double lastCandleOpenPrice = 300;
        candles.add(new CandleBuilder().setOpenPrice(lastCandleOpenPrice).build());

        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(candles);

        AssertUtils.assertEquals(lastCandleOpenPrice, decisionData.getCurrentPrice());
    }

    // endregion

}