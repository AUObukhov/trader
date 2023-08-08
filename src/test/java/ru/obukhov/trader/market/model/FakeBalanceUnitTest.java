package ru.obukhov.trader.market.model;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;

class FakeBalanceUnitTest {

    @Test
    void constructor_initializesFields() {
        final FakeBalance fakeBalance = new FakeBalance();

        AssertUtils.assertEquals(0, fakeBalance.getCurrentAmount());
        Assertions.assertTrue(fakeBalance.getInvestments().isEmpty());
    }

    // region addInvestment tests

    @Test
    void addInvestment_changesInvestmentsAndCurrentBalance_whenAmountIsPositive() {
        final BigDecimal amount1 = BigDecimal.valueOf(20);
        final BigDecimal amount2 = BigDecimal.valueOf(50);
        final BigDecimal amount3 = BigDecimal.valueOf(40);
        final Timestamp investment1DateTime = TimestampUtils.now();
        final Timestamp investment2DateTime = TimestampUtils.plusHours(investment1DateTime, 1);
        final Timestamp investment3DateTime = TimestampUtils.plusHours(investment1DateTime, 1);

        final FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);
        fakeBalance.addInvestment(investment3DateTime, amount3);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        AssertUtils.assertEquals(20, fakeBalance.getInvestments().get(investment1DateTime));
        AssertUtils.assertEquals(90, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(110, fakeBalance.getCurrentAmount());
    }

    @Test
    void addInvestment_changesInvestmentsAndDecreasesCurrentBalance_whenAmountIsNegative() {
        final BigDecimal amount1 = BigDecimal.valueOf(20);
        final BigDecimal amount2 = BigDecimal.valueOf(-50);
        final BigDecimal amount3 = BigDecimal.valueOf(10);
        final Timestamp investment1DateTime = TimestampUtils.now();
        final Timestamp investment2DateTime = TimestampUtils.plusHours(investment1DateTime, 1);
        final Timestamp investment3DateTime = TimestampUtils.plusHours(investment1DateTime, 1);

        final FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);
        fakeBalance.addInvestment(investment3DateTime, amount3);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        AssertUtils.assertEquals(20, fakeBalance.getInvestments().get(investment1DateTime));
        AssertUtils.assertEquals(-40, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(-20, fakeBalance.getCurrentAmount());
    }

    @Test
    void addInvestment_changesInvestmentsButNotChangesCurrentBalance_whenAmountIsZero() {
        final BigDecimal amount1 = BigDecimal.valueOf(20);
        final BigDecimal amount2 = BigDecimal.ZERO;
        final BigDecimal amount3 = BigDecimal.valueOf(10);
        final Timestamp investment1DateTime = TimestampUtils.now();
        final Timestamp investment2DateTime = TimestampUtils.plusHours(investment1DateTime, 1);
        final Timestamp investment3DateTime = investment1DateTime;

        final FakeBalance fakeBalance = new FakeBalance();

        fakeBalance.addInvestment(investment1DateTime, amount1);
        fakeBalance.addInvestment(investment2DateTime, amount2);
        fakeBalance.addInvestment(investment3DateTime, amount3);

        Assertions.assertEquals(2, fakeBalance.getInvestments().size());
        AssertUtils.assertEquals(30, fakeBalance.getInvestments().get(investment1DateTime));
        AssertUtils.assertEquals(0, fakeBalance.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(30, fakeBalance.getCurrentAmount());
    }

    // endregion

}