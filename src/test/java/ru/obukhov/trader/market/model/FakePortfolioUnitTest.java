package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FakePortfolioUnitTest {

    @Test
    void constructor_initsFields() {
        final String brokerAccountId = "2000124699";

        final FakePortfolio portfolio = new FakePortfolio(brokerAccountId);

        Assertions.assertEquals(brokerAccountId, portfolio.getBrokerAccountId());
        Assertions.assertNotNull(portfolio.getBalances());
        Assertions.assertNotNull(portfolio.getTickersToPositions());
        Assertions.assertNotNull(portfolio.getOperations());
    }

}