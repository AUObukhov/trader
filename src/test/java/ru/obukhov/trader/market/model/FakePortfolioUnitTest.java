package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.TestData;

class FakePortfolioUnitTest {

    @Test
    void constructor_initsFields() {
        final String accountId = TestData.ACCOUNT_ID1;

        final FakePortfolio portfolio = new FakePortfolio(accountId);

        Assertions.assertEquals(accountId, portfolio.getAccountId());
        Assertions.assertNotNull(portfolio.getBalances());
        Assertions.assertNotNull(portfolio.getFigiesToPositions());
        Assertions.assertNotNull(portfolio.getOperations());
    }

}