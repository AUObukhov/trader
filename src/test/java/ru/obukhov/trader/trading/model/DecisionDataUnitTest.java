package ru.obukhov.trader.trading.model;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.market.model.PositionBuilder;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.piapi.core.models.Position;

class DecisionDataUnitTest {

    @Test
    void getQuantity() {
        final long quantity = 30;

        final Position portfolioPosition = new PositionBuilder()
                .setQuantity(quantity)
                .build();
        final DecisionData decisionData = new DecisionData().setPosition(portfolioPosition);

        AssertUtils.assertEquals(quantity, decisionData.getQuantity());

    }

}