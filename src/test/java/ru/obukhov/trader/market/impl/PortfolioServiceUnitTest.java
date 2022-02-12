package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestData;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceUnitTest {

    @Mock
    private TinkoffService tinkoffService;

    @InjectMocks
    private PortfolioService service;

    // region getPosition tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPosition_returnsPositionByTicker_whenItExists(@Nullable final String brokerAccountId) {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final List<PortfolioPosition> positions = List.of(
                TestData.createPortfolioPosition(ticker1),
                TestData.createPortfolioPosition(ticker2),
                TestData.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions(brokerAccountId)).thenReturn(positions);

        final PortfolioPosition position = service.getPosition(brokerAccountId, ticker2);

        Assertions.assertEquals(ticker2, position.getTicker());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPosition_returnsNull_whenNoPositionWithTicker(@Nullable final String brokerAccountId) {
        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final List<PortfolioPosition> positions = List.of(
                TestData.createPortfolioPosition(ticker1),
                TestData.createPortfolioPosition(ticker2),
                TestData.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions(brokerAccountId)).thenReturn(positions);

        final PortfolioPosition position = service.getPosition(brokerAccountId, "ticker");

        Assertions.assertNull(position);
    }

    // endregion

    // region getAvailableBalance tests

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists(@Nullable final String brokerAccountId) {
        final long rubBalance = 1000;
        final long rubBlocked = 100;

        final List<CurrencyPosition> currencies = List.of(
                TestData.createCurrencyPosition(Currency.USD, 100),
                TestData.createCurrencyPosition(Currency.RUB, rubBalance, rubBlocked),
                TestData.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies(brokerAccountId)).thenReturn(currencies);

        final BigDecimal balance = service.getAvailableBalance(brokerAccountId, Currency.RUB);

        AssertUtils.assertEquals(rubBalance - rubBlocked, balance);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getAvailableBalance_returnsBalance_whenNoBlocked(@Nullable final String brokerAccountId) {
        final long rubBalance = 1000;

        final List<CurrencyPosition> currencies = List.of(
                TestData.createCurrencyPosition(Currency.USD, 100),
                TestData.createCurrencyPosition(Currency.RUB, rubBalance),
                TestData.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies(brokerAccountId)).thenReturn(currencies);

        final BigDecimal balance = service.getAvailableBalance(brokerAccountId, Currency.RUB);

        AssertUtils.assertEquals(rubBalance, balance);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency(@Nullable final String brokerAccountId) {
        final List<CurrencyPosition> currencies = List.of(
                TestData.createCurrencyPosition(Currency.USD, 100),
                TestData.createCurrencyPosition(Currency.EUR, 10)
        );
        Mockito.when(tinkoffService.getPortfolioCurrencies(brokerAccountId)).thenReturn(currencies);

        final Executable executable = () -> service.getAvailableBalance(brokerAccountId, Currency.RUB);
        Assertions.assertThrows(NoSuchElementException.class, executable, "No value present");
    }

    // endregion

}