package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
    void addInvestment_throwsIllegalArgumentException_whenAmountIsNegative() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final BigDecimal amount = BigDecimal.valueOf(-20);

        final FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertThrowsWithMessage(
                () -> fakeBalance.addInvestment(currentDateTime, amount),
                IllegalArgumentException.class,
                "expected positive investment amount"
        );
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsZero() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final BigDecimal amount = BigDecimal.ZERO;

        final FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertThrowsWithMessage(
                () -> fakeBalance.addInvestment(currentDateTime, amount),
                IllegalArgumentException.class,
                "expected positive investment amount"
        );
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenInvestmentWithCurrentDateTimeAlreadyExists() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final BigDecimal amount1 = BigDecimal.valueOf(100);
        final BigDecimal amount2 = BigDecimal.valueOf(20);

        final FakeBalance fakeBalance = new FakeBalance();
        fakeBalance.addInvestment(currentDateTime, amount1);

        AssertUtils.assertThrowsWithMessage(
                () -> fakeBalance.addInvestment(currentDateTime, amount2),
                IllegalArgumentException.class,
                "investment at " + currentDateTime + " alreadyExists"
        );
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

    // endregion

}
