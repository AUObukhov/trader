package ru.obukhov.trader.market.model;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

class FakeContextUnitTest {

    @Test
    void constructor_initializesFields() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final FakeContext fakeContext = new FakeContext(currentDateTime);

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());
        for (final Currency currency : Currency.values()) {
            Assertions.assertEquals(0, fakeContext.getInvestments(currency).size());
        }
        Assertions.assertTrue(fakeContext.getOperations().isEmpty());
        Assertions.assertTrue(fakeContext.getPositions().isEmpty());
    }

    // region addInvestment tests

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsNegative() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(-20);
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        AssertUtils.assertThrowsWithMessage(
                () -> fakeContext.addInvestment(currency, investment),
                IllegalArgumentException.class,
                "expected positive investment amount"
        );
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsZero() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.ZERO;
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        AssertUtils.assertThrowsWithMessage(
                () -> fakeContext.addInvestment(currency, investment),
                IllegalArgumentException.class,
                "expected positive investment amount"
        );
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenInvestmentWithCurrentDateTimeAlreadyExists() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(20);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.addInvestment(currency, balance);

        AssertUtils.assertThrowsWithMessage(
                () -> fakeContext.addInvestment(currency, investment),
                IllegalArgumentException.class,
                "investment at " + currentDateTime + " alreadyExists"
        );
    }

    @Test
    void addInvestment_changesInvestmentsAndCurrentBalance() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment1 = BigDecimal.valueOf(20);
        final BigDecimal investment2 = BigDecimal.valueOf(50);
        final OffsetDateTime investment1DateTime = currentDateTime.plusHours(1);
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(currency, balance);

        fakeContext.setCurrentDateTime(investment1DateTime);
        fakeContext.addInvestment(currency, investment1);

        fakeContext.setCurrentDateTime(investment2DateTime);
        fakeContext.addInvestment(currency, investment2);

        Assertions.assertEquals(2, fakeContext.getInvestments(currency).size());
        Assertions.assertEquals(investment1, fakeContext.getInvestments(currency).get(investment1DateTime));
        Assertions.assertEquals(investment2, fakeContext.getInvestments(currency).get(investment2DateTime));

        AssertUtils.assertEquals(170, fakeContext.getBalance(currency));
    }

    // endregion

    @Test
    void addOperation_addsOperation_and_getOperationsReturnsOperations() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(currency, balance);

        final SimulatedOperation operation = new SimulatedOperation(null,
                DateUtils.getDateTime(2021, 1, 1, 10, 0, 0),
                null,
                null,
                null,
                null);
        fakeContext.addOperation(operation);

        final Set<SimulatedOperation> operations = fakeContext.getOperations();
        Assertions.assertEquals(1, operations.size());
        Assertions.assertSame(operation, operations.iterator().next());
    }

    @Test
    void addPosition_addsPosition_and_getPosition_returnsPosition() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = createPosition();

        fakeContext.addPosition(ticker, position);
        PortfolioPosition readPosition = fakeContext.getPosition(ticker);

        Assertions.assertSame(position, readPosition);
    }

    @Test
    void removePosition_removesPosition() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = createPosition();

        fakeContext.addPosition(ticker, position);
        fakeContext.removePosition(ticker);
        Assertions.assertTrue(fakeContext.getPositions().isEmpty());
    }

    @NotNull
    private PortfolioPosition createPosition() {
        return new PortfolioPosition(
                null,
                BigDecimal.ZERO,
                null,
                Currency.RUB,
                null,
                0,
                null,
                null,
                StringUtils.EMPTY
        );
    }

}