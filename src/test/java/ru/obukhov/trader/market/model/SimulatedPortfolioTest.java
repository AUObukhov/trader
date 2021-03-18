package ru.obukhov.trader.market.model;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.web.model.pojo.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

class SimulatedPortfolioTest {

    @Test
    void constructor_initializesPortfolio() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        Assertions.assertEquals(currentDateTime, portfolio.getCurrentDateTime());

        Assertions.assertEquals(balance, portfolio.getCurrentBalance());

        Assertions.assertEquals(1, portfolio.getInvestments().size());
        Assertions.assertEquals(balance, portfolio.getInvestments().get(currentDateTime));

        Assertions.assertTrue(portfolio.getOperations().isEmpty());
        Assertions.assertTrue(portfolio.getPositions().isEmpty());
    }

    // region addInvestment tests

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsNegative() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);
        BigDecimal investment = BigDecimal.valueOf(-20);
        OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        portfolio.setCurrentDateTime(investmentDateTime);

        AssertUtils.assertThrowsWithMessage(() -> portfolio.addInvestment(investment),
                IllegalArgumentException.class,
                "expected positive investment amount");
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenAmountIsZero() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);
        BigDecimal investment = BigDecimal.ZERO;
        OffsetDateTime investmentDateTime = currentDateTime.plusHours(1);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        portfolio.setCurrentDateTime(investmentDateTime);

        AssertUtils.assertThrowsWithMessage(() -> portfolio.addInvestment(investment),
                IllegalArgumentException.class,
                "expected positive investment amount");
    }

    @Test
    void addInvestment_throwsIllegalArgumentException_whenInvestmentWithCurrentDateTimeAlreadyExists() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);
        BigDecimal investment = BigDecimal.valueOf(20);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        AssertUtils.assertThrowsWithMessage(() -> portfolio.addInvestment(investment),
                IllegalArgumentException.class,
                "investment at " + currentDateTime + " alreadyExists");
    }

    @Test
    void addInvestment_changesInvestmentsAndCurrentBalance() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);
        BigDecimal investment1 = BigDecimal.valueOf(20);
        BigDecimal investment2 = BigDecimal.valueOf(50);
        OffsetDateTime investment1DateTime = currentDateTime.plusHours(1);
        OffsetDateTime investment2DateTime = investment1DateTime.plusHours(1);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        portfolio.setCurrentDateTime(investment1DateTime);
        portfolio.addInvestment(investment1);

        portfolio.setCurrentDateTime(investment2DateTime);
        portfolio.addInvestment(investment2);

        Assertions.assertEquals(3, portfolio.getInvestments().size());
        Assertions.assertEquals(balance, portfolio.getInvestments().get(currentDateTime));
        Assertions.assertEquals(investment1, portfolio.getInvestments().get(investment1DateTime));
        Assertions.assertEquals(investment2, portfolio.getInvestments().get(investment2DateTime));

        AssertUtils.assertEquals(BigDecimal.valueOf(170), portfolio.getCurrentBalance());
    }

    // endregion

    @Test
    void addOperation_addsOperation_and_getOperationsReturnsOperations() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        SimulatedOperation operation = new SimulatedOperation(null,
                DateUtils.getDateTime(2021, 1, 1, 10, 0, 0),
                null,
                null,
                null,
                null);
        portfolio.addOperation(operation);

        Set<SimulatedOperation> operations = portfolio.getOperations();
        Assertions.assertEquals(1, operations.size());
        Assertions.assertSame(operation, operations.iterator().next());
    }

    @Test
    void addPosition_addsPosition_and_getPosition_returnsPosition() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        String ticker = "ticker";
        PortfolioPosition position = createPosition();

        portfolio.addPosition(ticker, position);
        PortfolioPosition readPosition = portfolio.getPosition(ticker);

        Assertions.assertSame(position, readPosition);
    }

    @Test
    void removePosition_removesPosition() {
        OffsetDateTime currentDateTime = OffsetDateTime.now();
        BigDecimal balance = BigDecimal.valueOf(100);

        SimulatedPortfolio portfolio = new SimulatedPortfolio(currentDateTime, balance);

        String ticker = "ticker";
        PortfolioPosition position = createPosition();

        portfolio.addPosition(ticker, position);
        portfolio.removePosition(ticker);
        Assertions.assertTrue(portfolio.getPositions().isEmpty());
    }

    @NotNull
    private PortfolioPosition createPosition() {
        return new PortfolioPosition(null,
                BigDecimal.ZERO,
                null,
                Currency.RUB,
                null,
                0,
                null,
                null,
                StringUtils.EMPTY);
    }


}