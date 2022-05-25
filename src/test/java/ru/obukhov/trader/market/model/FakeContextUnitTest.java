package ru.obukhov.trader.market.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.BackTestOperation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Stream;

class FakeContextUnitTest {

    @Test
    void constructor_withoutInitialBalance_initializesCurrentDateTime() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final FakeContext fakeContext = new FakeContext(currentDateTime);

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());
    }

    // region constructor with initialBalance tests

    @Test
    void constructor_withInitialBalance_initializesCurrentDateTime() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime, accountId, currency, balance);

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConstructor_withInitialBalance_initializesBalance() {
        return Stream.of(
                Arguments.of("2000124699", 100),
                Arguments.of("2000124699", -100),
                Arguments.of("2000124699", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forConstructor_withInitialBalance_initializesBalance")
    void constructor_withInitialBalance_initializesBalance(final int balance) {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;

        final FakeContext fakeContext = new FakeContext(currentDateTime, accountId, currency, DecimalUtils.setDefaultScale(balance));

        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    // region addInvestment without dateTime tests

    @Test
    void addInvestment_withoutDateTime_changesInvestmentsAndCurrentBalance() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment1 = BigDecimal.valueOf(20);
        final BigDecimal investment2 = BigDecimal.valueOf(50);
        final BigDecimal investment3 = BigDecimal.valueOf(30);
        final OffsetDateTime investment1DateTime = currentDateTime.plusHours(1);
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        fakeContext.setCurrentDateTime(investment1DateTime);
        fakeContext.addInvestment(accountId, currency, investment1);

        fakeContext.setCurrentDateTime(investment2DateTime);
        fakeContext.addInvestment(accountId, currency, investment2);
        fakeContext.addInvestment(accountId, currency, investment3);

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(20, fakeContext.getInvestments(accountId, currency).get(investment1DateTime));
        AssertUtils.assertEquals(80, fakeContext.getInvestments(accountId, currency).get(investment2DateTime));

        AssertUtils.assertEquals(200, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withoutDateTime_subtractsBalance_whenAmountIsNegative() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(-20);
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        fakeContext.addInvestment(accountId, currency, investment);

        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(80, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withoutDateTime_notChangesBalance_whenAmountIsZero() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.ZERO;
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        fakeContext.addInvestment(accountId, currency, investment);

        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    // region addInvestment with dateTime tests

    @Test
    void addInvestment_withDateTime_changesInvestmentsAndCurrentBalance() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment1 = BigDecimal.valueOf(20);
        final BigDecimal investment2 = BigDecimal.valueOf(50);
        final BigDecimal investment3 = BigDecimal.valueOf(30);
        final OffsetDateTime investment1DateTime = currentDateTime.plusHours(1);
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        fakeContext.addInvestment(accountId, investment1DateTime, currency, investment1);

        fakeContext.addInvestment(accountId, investment2DateTime, currency, investment2);
        fakeContext.addInvestment(accountId, investment2DateTime, currency, investment3);

        Assertions.assertEquals(2, fakeContext.getInvestments(accountId, currency).size());
        AssertUtils.assertEquals(20, fakeContext.getInvestments(accountId, currency).get(investment1DateTime));
        AssertUtils.assertEquals(80, fakeContext.getInvestments(accountId, currency).get(investment2DateTime));

        AssertUtils.assertEquals(200, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withDateTime_throwsIllegalArgumentException_whenAmountIsNegative() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(-20);
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        fakeContext.addInvestment(accountId, investmentDateTime, currency, investment);

        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(80, fakeContext.getBalance(accountId, currency));
    }

    @Test
    void addInvestment_withDateTime_throwsIllegalArgumentException_whenAmountIsZero() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.ZERO;
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        fakeContext.addInvestment(accountId, investmentDateTime, currency, investment);

        Assertions.assertEquals(1, fakeContext.getInvestments(accountId, currency).size());
        Assertions.assertEquals(investment, fakeContext.getInvestments(accountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance, fakeContext.getBalance(accountId, currency));
    }

    // endregion

    @Test
    void addOperation_addsOperation_and_getOperationsReturnsOperations() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        final BackTestOperation operation = new BackTestOperation(null,
                DateTimeTestData.createDateTime(2021, 1, 1, 10),
                null,
                null,
                null
        );
        fakeContext.addOperation(accountId, operation);

        final Set<BackTestOperation> operations = fakeContext.getOperations(accountId);
        Assertions.assertEquals(1, operations.size());
        Assertions.assertSame(operation, operations.iterator().next());
    }

    @Test
    void addPosition_addsPosition_and_getPosition_returnsPosition() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = TestData.createPortfolioPosition();

        fakeContext.addPosition(accountId, ticker, position);
        PortfolioPosition readPosition = fakeContext.getPosition(accountId, ticker);

        Assertions.assertSame(position, readPosition);
    }

    @Test
    void removePosition_removesPosition() {
        final String accountId = "2000124699";

        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(accountId, currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = TestData.createPortfolioPosition();

        fakeContext.addPosition(accountId, ticker, position);
        fakeContext.removePosition(accountId, ticker);
        Assertions.assertTrue(fakeContext.getPositions(accountId).isEmpty());
    }

}