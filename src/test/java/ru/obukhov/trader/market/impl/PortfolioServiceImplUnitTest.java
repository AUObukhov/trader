package ru.obukhov.trader.market.impl;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import ru.obukhov.trader.BaseMockedTest;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@RunWith(MockitoJUnitRunner.class)
class PortfolioServiceImplUnitTest extends BaseMockedTest {

    private static final String TICKER = "ticker";

    @Mock
    private TinkoffService tinkoffService;

    private PortfolioServiceImpl service;

    @BeforeEach
    public void setUp() {
        this.service = new PortfolioServiceImpl(tinkoffService);
    }

    // region getPosition tests

    @Test
    void getPosition_returnsPositionByTicker_whenItExists() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";
        String ticker3 = "ticker3";

        List<PortfolioPosition> positions = ImmutableList.of(
                TestDataHelper.createPortfolioPosition(ticker1),
                TestDataHelper.createPortfolioPosition(ticker2),
                TestDataHelper.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions()).thenReturn(positions);

        PortfolioPosition position = service.getPosition(ticker2);

        Assertions.assertEquals(ticker2, position.getTicker());
    }

    @Test
    void getPosition_returnsNull_whenNoPositionWithTicker() {
        String ticker1 = "ticker1";
        String ticker2 = "ticker2";
        String ticker3 = "ticker3";

        List<PortfolioPosition> positions = ImmutableList.of(
                TestDataHelper.createPortfolioPosition(ticker1),
                TestDataHelper.createPortfolioPosition(ticker2),
                TestDataHelper.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions()).thenReturn(positions);

        PortfolioPosition position = service.getPosition(TICKER);

        Assertions.assertNull(position);
    }

    // endregion

    // region getAvailableBalance tests

    @Test
    void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists() {

        long rubBalance = 1000;
        long rubBlocked = 100;

        List<CurrencyPosition> currencies = ImmutableList.of(
                TestDataHelper.createCurrencyPosition(Currency.USD, 100),
                TestDataHelper.createCurrencyPosition(Currency.RUB, rubBalance, rubBlocked),
                TestDataHelper.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        BigDecimal balance = service.getAvailableBalance(Currency.RUB);

        AssertUtils.assertEquals(rubBalance - rubBlocked, balance);

    }

    @Test
    void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {

        List<CurrencyPosition> currencies = ImmutableList.of(
                TestDataHelper.createCurrencyPosition(Currency.USD, 100),
                TestDataHelper.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        AssertUtils.assertThrowsWithMessage(() -> service.getAvailableBalance(Currency.RUB),
                NoSuchElementException.class,
                "No value present");

    }

    // endregion

}