package ru.obukhov.trader.market.model;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.DateTimeTestData;
import ru.obukhov.trader.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

class FakeContextUnitTest {

    @Test
    void constructor_initializesCurrentDateTime() {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();

        final FakeContext fakeContext = new FakeContext(currentDateTime);

        Assertions.assertEquals(currentDateTime, fakeContext.getCurrentDateTime());
    }

    // region addInvestment tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void addInvestment_throwsIllegalArgumentException_whenInvestmentWithCurrentDateTimeAlreadyExists(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(20);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.addInvestment(brokerAccountId, currency, balance);

        final Executable executable = () -> fakeContext.addInvestment(brokerAccountId, currency, investment);
        final String expectedMessage = "investment at " + currentDateTime + " alreadyExists";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void addInvestment_changesInvestmentsAndCurrentBalance(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment1 = BigDecimal.valueOf(20);
        final BigDecimal investment2 = BigDecimal.valueOf(50);
        final OffsetDateTime investment1DateTime = currentDateTime.plusHours(1);
        final OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(brokerAccountId, currency, balance);

        fakeContext.setCurrentDateTime(investment1DateTime);
        fakeContext.addInvestment(brokerAccountId, currency, investment1);

        fakeContext.setCurrentDateTime(investment2DateTime);
        fakeContext.addInvestment(brokerAccountId, currency, investment2);

        Assertions.assertEquals(2, fakeContext.getInvestments(brokerAccountId, currency).size());
        Assertions.assertEquals(investment1, fakeContext.getInvestments(brokerAccountId, currency).get(investment1DateTime));
        Assertions.assertEquals(investment2, fakeContext.getInvestments(brokerAccountId, currency).get(investment2DateTime));

        AssertUtils.assertEquals(170, fakeContext.getBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void addInvestment_throwsIllegalArgumentException_whenAmountIsNegative(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.valueOf(-20);
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(brokerAccountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        fakeContext.addInvestment(brokerAccountId, currency, investment);

        Assertions.assertEquals(1, fakeContext.getInvestments(brokerAccountId, currency).size());
        Assertions.assertEquals(investment, fakeContext.getInvestments(brokerAccountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(80, fakeContext.getBalance(brokerAccountId, currency));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void addInvestment_throwsIllegalArgumentException_whenAmountIsZero(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);
        final BigDecimal investment = BigDecimal.ZERO;
        final OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(brokerAccountId, currency, balance);

        fakeContext.setCurrentDateTime(investmentDateTime);

        fakeContext.addInvestment(brokerAccountId, currency, investment);

        Assertions.assertEquals(1, fakeContext.getInvestments(brokerAccountId, currency).size());
        Assertions.assertEquals(investment, fakeContext.getInvestments(brokerAccountId, currency).get(investmentDateTime));

        AssertUtils.assertEquals(balance, fakeContext.getBalance(brokerAccountId, currency));
    }

    // endregion

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void addOperation_addsOperation_and_getOperationsReturnsOperations(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(brokerAccountId, currency, balance);

        final SimulatedOperation operation = new SimulatedOperation(null,
                DateTimeTestData.createDateTime(2021, 1, 1, 10),
                null,
                null,
                null,
                null);
        fakeContext.addOperation(brokerAccountId, operation);

        final Set<SimulatedOperation> operations = fakeContext.getOperations(brokerAccountId);
        Assertions.assertEquals(1, operations.size());
        Assertions.assertSame(operation, operations.iterator().next());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void addPosition_addsPosition_and_getPosition_returnsPosition(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(brokerAccountId, currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = createPosition();

        fakeContext.addPosition(brokerAccountId, ticker, position);
        PortfolioPosition readPosition = fakeContext.getPosition(brokerAccountId, ticker);

        Assertions.assertSame(position, readPosition);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void removePosition_removesPosition(@Nullable final String brokerAccountId) {
        final OffsetDateTime currentDateTime = OffsetDateTime.now();
        final Currency currency = Currency.RUB;
        final BigDecimal balance = BigDecimal.valueOf(100);

        final FakeContext fakeContext = new FakeContext(currentDateTime);
        fakeContext.setCurrentBalance(brokerAccountId, currency, balance);

        final String ticker = "ticker";
        PortfolioPosition position = createPosition();

        fakeContext.addPosition(brokerAccountId, ticker, position);
        fakeContext.removePosition(brokerAccountId, ticker);
        Assertions.assertTrue(fakeContext.getPositions(brokerAccountId).isEmpty());
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