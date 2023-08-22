package ru.obukhov.trader.market.model;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

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
        final Quotation amount1 = QuotationUtils.newQuotation(20L);
        final Quotation amount2 = QuotationUtils.newQuotation(50L);
        final Quotation amount3 = QuotationUtils.newQuotation(40L);
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
        final Quotation amount1 = QuotationUtils.newQuotation(20L);
        final Quotation amount2 = QuotationUtils.newQuotation(-50L);
        final Quotation amount3 = QuotationUtils.newQuotation(10L);
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
        final Quotation amount1 = QuotationUtils.newQuotation(20L);
        final Quotation amount2 = QuotationUtils.ZERO;
        final Quotation amount3 = QuotationUtils.newQuotation(10L);
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