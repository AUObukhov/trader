package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

class FakeBalanceTest {

    @Test
    void constructor_initializesFields() {
        FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertEquals(BigDecimal.ZERO, fakeBalance.getCurrentAmount());
        Assertions.assertTrue(fakeBalance.getInvestments().isEmpty());
    }

    // region addInvestment tests

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsNegative() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal amount = BigDecimal.valueOf(-20);

        FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertThrowsWithMessage(() -> fakeBalance.addInvestment(currentDateTime, amount),
                IllegalArgumentException.class,
                "expected positive investment amount");
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsZero() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal amount = BigDecimal.ZERO;

        FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertThrowsWithMessage(() -> fakeBalance.addInvestment(currentDateTime, amount),
                IllegalArgumentException.class,
                "expected positive investment amount");
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenInvestmentWithCurrentDateTimeAlreadyExists() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal amount1 = BigDecimal.valueOf(100);
        BigDecimal amount2 = BigDecimal.valueOf(20);

        FakeBalance fakeBalance = new FakeBalance();
        fakeBalance.addInvestment(currentDateTime, amount1);

        AssertUtils.assertThrowsWithMessage(() -> fakeBalance.addInvestment(currentDateTime, amount2),
                IllegalArgumentException.class,
                "investment at " + currentDateTime + " alreadyExists");
    }

    @Test
    void addInvestment_changesInvestmentsAndCurrentBalance() {
        BigDecimal amount1 = BigDecimal.valueOf(20);
        BigDecimal amount2 = BigDecimal.valueOf(50);
        OffsetDateTime investment1DateTime = OffsetDateTime.now();
        OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        Assertions.assertEquals(amount1, fakeBalance.getInvestments().get(investment1DateTime));
        Assertions.assertEquals(amount2, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(BigDecimal.valueOf(70), fakeBalance.getCurrentAmount());
    }

    // endregion

}
