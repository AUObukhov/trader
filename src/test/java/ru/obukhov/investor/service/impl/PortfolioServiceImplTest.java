package ru.obukhov.investor.service.impl;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.service.interfaces.ConnectionService;
import ru.obukhov.investor.service.interfaces.PortfolioService;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.PortfolioContext;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.portfolio.InstrumentType;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioServiceImplTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    @Mock
    private ConnectionService connectionService;
    @Mock
    private PortfolioContext portfolioContext;

    private PortfolioService service;

    @Before
    public void setUp() {
        when(connectionService.getPortfolioContext()).thenReturn(portfolioContext);

        this.service = new PortfolioServiceImpl(connectionService);
    }

    // region getPosition tests

    @Test
    public void getPosition_returnsPositionByTicker_whenItExists() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";
        String ticker3 = "ticker3";

        List<Portfolio.PortfolioPosition> positions = ImmutableList.of(
                createPortfolioPosition(ticker1),
                createPortfolioPosition(ticker2),
                createPortfolioPosition(ticker3)
        );
        mockPortfolio(positions);

        Portfolio.PortfolioPosition position = service.getPosition(ticker2);

        assertEquals(ticker2, position.ticker);
    }

    @Test
    public void getPosition_returnsNull_whenNoPositionWithTicker() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";
        String ticker3 = "ticker3";

        List<Portfolio.PortfolioPosition> positions = ImmutableList.of(
                createPortfolioPosition(ticker1),
                createPortfolioPosition(ticker2),
                createPortfolioPosition(ticker3)
        );
        mockPortfolio(positions);

        Portfolio.PortfolioPosition position = service.getPosition(TICKER);

        assertNull(position);
    }

    // endregion

    // region getAvailableBalance tests

    @Test
    public void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists() {

        int rubBalance = 1000;
        int rubBlocked = 100;

        List<PortfolioCurrencies.PortfolioCurrency> currencies = ImmutableList.of(
                createPortfolioCurrency(Currency.USD, 100, 0),
                createPortfolioCurrency(Currency.RUB, rubBalance, rubBlocked),
                createPortfolioCurrency(Currency.EUR, 10, 0)
        );
        mockCurrencies(currencies);

        BigDecimal balance = service.getAvailableBalance(Currency.RUB);

        assertTrue(MathUtils.numbersEqual(balance, rubBalance - rubBlocked));

    }

    @Test(expected = NoSuchElementException.class)
    public void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {

        List<PortfolioCurrencies.PortfolioCurrency> currencies = ImmutableList.of(
                createPortfolioCurrency(Currency.USD, 100, 0),
                createPortfolioCurrency(Currency.EUR, 10, 0)
        );
        mockCurrencies(currencies);

        service.getAvailableBalance(Currency.RUB);

    }

    // endregion

    // region mocks

    private void mockPortfolio(List<Portfolio.PortfolioPosition> positions) {
        Portfolio portfolio = new Portfolio(positions);
        CompletableFuture<Portfolio> future = CompletableFuture.completedFuture(portfolio);
        when(portfolioContext.getPortfolio(isNull())).thenReturn(future);
    }

    private Portfolio.PortfolioPosition createPortfolioPosition(String ticker) {
        return new Portfolio.PortfolioPosition(StringUtils.EMPTY,
                ticker,
                null,
                InstrumentType.Bond,
                BigDecimal.ONE,
                null,
                null,
                1,
                null,
                null,
                StringUtils.EMPTY
        );
    }

    private void mockCurrencies(List<PortfolioCurrencies.PortfolioCurrency> currencies) {
        PortfolioCurrencies portfolioCurrencies = new PortfolioCurrencies(currencies);
        CompletableFuture<PortfolioCurrencies> future = CompletableFuture.completedFuture(portfolioCurrencies);
        when(portfolioContext.getPortfolioCurrencies(isNull())).thenReturn(future);
    }

    private PortfolioCurrencies.PortfolioCurrency createPortfolioCurrency(Currency currency, int balance, int blocked) {
        return new PortfolioCurrencies.PortfolioCurrency(currency,
                BigDecimal.valueOf(balance),
                BigDecimal.valueOf(blocked));
    }

    // endregion

}