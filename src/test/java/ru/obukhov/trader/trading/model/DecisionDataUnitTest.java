package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.tinkoff.piapi.core.models.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DecisionDataUnitTest {

    @Test
    void getQuantityLots() {
        final long quantityLots = 30;

        final Position portfolioPosition = new PositionBuilder()
                .setQuantityLots(quantityLots)
                .build();
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
    void getCurrentPrice_returnsLastCandleOpen() {
        final List<Candle> candles = new ArrayList<>();
        candles.add(new CandleBuilder().setOpen(100).build());
        candles.add(new CandleBuilder().setOpen(200).build());
        final double lastCandleOpen = 300;
        candles.add(new CandleBuilder().setOpen(lastCandleOpen).build());

        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(candles);

        AssertUtils.assertEquals(lastCandleOpen, decisionData.getCurrentPrice());
    }

    // endregion

}