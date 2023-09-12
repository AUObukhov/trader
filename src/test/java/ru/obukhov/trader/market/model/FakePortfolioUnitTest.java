package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;

class FakePortfolioUnitTest {

    @Test
    void constructor_initializesFields() {
        final String accountId = TestAccounts.TINKOFF.account().id();

        final FakePortfolio portfolio = new FakePortfolio(accountId);

        Assertions.assertEquals(accountId, portfolio.getAccountId());
        Assertions.assertNotNull(portfolio.getBalances());
        Assertions.assertNotNull(portfolio.getFigiesToPositions());
        Assertions.assertNotNull(portfolio.getOperations());
    }

}