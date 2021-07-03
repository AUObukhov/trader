package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceImplUnitTest {

    @Mock
    private TinkoffService tinkoffService;

    @InjectMocks
    private PortfolioServiceImpl service;

    // region getPosition tests

    @Test
    void getPosition_returnsPositionByTicker_whenItExists() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final List<PortfolioPosition> positions = List.of(
                TestDataHelper.createPortfolioPosition(ticker1),
                TestDataHelper.createPortfolioPosition(ticker2),
                TestDataHelper.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions()).thenReturn(positions);

        final PortfolioPosition position = service.getPosition(ticker2);

        Assertions.assertEquals(ticker2, position.getTicker());
    }

    @Test
    void getPosition_returnsNull_whenNoPositionWithTicker() {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final List<PortfolioPosition> positions = List.of(
                TestDataHelper.createPortfolioPosition(ticker1),
                TestDataHelper.createPortfolioPosition(ticker2),
                TestDataHelper.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions()).thenReturn(positions);

        final PortfolioPosition position = service.getPosition("ticker");

        Assertions.assertNull(position);
    }

    // endregion

    // region getAvailableBalance tests

    @Test
    void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists() {
        final long rubBalance = 1000;
        final long rubBlocked = 100;

        final List<CurrencyPosition> currencies = List.of(
                TestDataHelper.createCurrencyPosition(Currency.USD, 100),
                TestDataHelper.createCurrencyPosition(Currency.RUB, rubBalance, rubBlocked),
                TestDataHelper.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        final BigDecimal balance = service.getAvailableBalance(Currency.RUB);

        AssertUtils.assertEquals(rubBalance - rubBlocked, balance);
    }

    @Test
    void getAvailableBalance_returnsBalance_whenNoBlocked() {
        final long rubBalance = 1000;

        final List<CurrencyPosition> currencies = List.of(
                TestDataHelper.createCurrencyPosition(Currency.USD, 100),
                TestDataHelper.createCurrencyPosition(Currency.RUB, rubBalance),
                TestDataHelper.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        final BigDecimal balance = service.getAvailableBalance(Currency.RUB);

        AssertUtils.assertEquals(rubBalance, balance);
    }

    @Test
    void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {
        final List<CurrencyPosition> currencies = List.of(
                TestDataHelper.createCurrencyPosition(Currency.USD, 100),
                TestDataHelper.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies()).thenReturn(currencies);

        AssertUtils.assertThrowsWithMessage(
                () -> service.getAvailableBalance(Currency.RUB),
                NoSuchElementException.class,
                "No value present"
        );
    }

    // endregion

}