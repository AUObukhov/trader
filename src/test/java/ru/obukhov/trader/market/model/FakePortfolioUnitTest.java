package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FakePortfolioUnitTest {

    @Test
    void constructor_initsFields() {
        final String accountId = "2000124699";

        final FakePortfolio portfolio = new FakePortfolio(accountId);

        Assertions.assertEquals(accountId, portfolio.getAccountId());
        Assertions.assertNotNull(portfolio.getBalances());
        Assertions.assertNotNull(portfolio.getTickersToPositions());
        Assertions.assertNotNull(portfolio.getOperations());
    }

}