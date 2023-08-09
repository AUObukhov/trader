package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class DecisionDataUnitTest {

    @Test
    void getQuantityLots() {
        final BigDecimal quantityLots = BigDecimal.valueOf(30);

        final Position portfolioPosition = Position.builder().quantityLots(quantityLots).build();
        final DecisionData decisionData = new DecisionData().setPosition(portfolioPosition);

        AssertUtils.assertEquals(quantityLots.longValue(), decisionData.getQuantityLots());

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