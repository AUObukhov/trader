package ru.obukhov.investor.common.service.impl;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.investor.BaseMockedTest;
import ru.obukhov.investor.common.util.MathUtils;
import ru.obukhov.investor.market.impl.PortfolioServiceImpl;
import ru.obukhov.investor.market.interfaces.PortfolioService;
import ru.obukhov.investor.market.interfaces.TinkoffService;
import ru.obukhov.investor.market.model.PortfolioPosition;
import ru.obukhov.investor.test.utils.AssertUtils;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortfolioServiceImplTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    @Mock
    private TinkoffService tinkoffService;

    private PortfolioService service;

    @Before
    public void setUp() {
        this.service = new PortfolioServiceImpl(tinkoffService);
    }

    // region getPosition tests

    @Test
    public void getPosition_returnsPositionByTicker_whenItExists() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";
        String ticker3 = "ticker3";

        List<PortfolioPosition> positions = ImmutableList.of(
                createPortfolioPosition(ticker1),
                createPortfolioPosition(ticker2),
                createPortfolioPosition(ticker3)
        );
        when(tinkoffService.getPortfolioPositions()).thenReturn(positions);

        PortfolioPosition position = service.getPosition(ticker2);

        assertEquals(ticker2, position.getTicker());
    }

    @Test
    public void getPosition_returnsNull_whenNoPositionWithTicker() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";
        String ticker3 = "ticker3";

        List<PortfolioPosition> positions = ImmutableList.of(
                createPortfolioPosition(ticker1),
                createPortfolioPosition(ticker2),
                createPortfolioPosition(ticker3)
        );
        when(tinkoffService.getPortfolioPositions()).thenReturn(positions);

        PortfolioPosition position = service.getPosition(TICKER);

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
        when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        BigDecimal balance = service.getAvailableBalance(Currency.RUB);

        assertTrue(MathUtils.numbersEqual(balance, rubBalance - rubBlocked));

    }

    @Test
    public void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {

        List<PortfolioCurrencies.PortfolioCurrency> currencies = ImmutableList.of(
                createPortfolioCurrency(Currency.USD, 100, 0),
                createPortfolioCurrency(Currency.EUR, 10, 0)
        );
        when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        AssertUtils.assertThrowsWithMessage(() -> service.getAvailableBalance(Currency.RUB),
                NoSuchElementException.class,
                "No value present");

    }

    // endregion

    // region mocks

    private PortfolioPosition createPortfolioPosition(String ticker) {
        return new PortfolioPosition(
                ticker,
                BigDecimal.ONE,
                null,
                Currency.RUB,
                null,
                1,
                null,
                null,
                StringUtils.EMPTY
        );
    }

    private PortfolioCurrencies.PortfolioCurrency createPortfolioCurrency(Currency currency, int balance, int blocked) {
        return new PortfolioCurrencies.PortfolioCurrency(currency,
                BigDecimal.valueOf(balance),
                BigDecimal.valueOf(blocked));
    }

    // endregion

}