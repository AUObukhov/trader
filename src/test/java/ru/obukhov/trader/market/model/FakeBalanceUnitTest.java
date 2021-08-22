package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class FakeBalanceUnitTest {

    @Test
    void constructor_initializesFields() {
        final FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertEquals(0, fakeBalance.getCurrentAmount());
        Assertions.assertTrue(fakeBalance.getInvestments().isEmpty());
    }

    // region addInvestment tests

    @Test
    void addInvestment_throwsIllegalArgumentException_whenInvestmentWithCurrentDateTimeAlreadyExists() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final BigDecimal amount1 = BigDecimal.valueOf(100);
        final BigDecimal amount2 = BigDecimal.valueOf(20);

        final FakeBalance fakeBalance = new FakeBalance();
        fakeBalance.addInvestment(currentDateTime, amount1);

        final Executable executable = () -> fakeBalance.addInvestment(currentDateTime, amount2);
        final String expectedMessage = "investment at " + currentDateTime + " alreadyExists";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void addInvestment_changesInvestmentsAndCurrentBalance() {
        final BigDecimal amount1 = BigDecimal.valueOf(20);
        final BigDecimal amount2 = BigDecimal.valueOf(50);
        final OffsetDateTime investment1DateTime = OffsetDateTime.now();
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        Assertions.assertEquals(amount1, fakeBalance.getInvestments().get(investment1DateTime));
        Assertions.assertEquals(amount2, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(70, fakeBalance.getCurrentAmount());
    }

    @Test
    void addInvestment_changesInvestmentsAndDecreasesCurrentBalance_whenAmountIsNegative() {
        final BigDecimal amount1 = BigDecimal.valueOf(20);
        final BigDecimal amount2 = BigDecimal.valueOf(-50);
        final OffsetDateTime investment1DateTime = OffsetDateTime.now();
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        Assertions.assertEquals(amount1, fakeBalance.getInvestments().get(investment1DateTime));
        Assertions.assertEquals(amount2, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(-30, fakeBalance.getCurrentAmount());
    }

    @Test
    void addInvestment_changesInvestmentsButNotChangesCurrentBalance_whenAmountIsZero() {
        final BigDecimal amount1 = BigDecimal.valueOf(20);
        final BigDecimal amount2 = BigDecimal.ZERO;
        final OffsetDateTime investment1DateTime = OffsetDateTime.now();
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        Assertions.assertEquals(amount1, fakeBalance.getInvestments().get(investment1DateTime));
        Assertions.assertEquals(amount2, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(20, fakeBalance.getCurrentAmount());
    }

    // endregion

}
