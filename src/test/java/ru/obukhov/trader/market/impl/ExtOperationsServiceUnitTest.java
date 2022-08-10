package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class ExtOperationsServiceUnitTest {

    @Mock
    private TinkoffService tinkoffService;

    @InjectMocks
    private ExtOperationsService extOperationsService;

    // region getPosition tests

    @Test
    void getPosition_returnsPositionByTicker_whenItExists() {
        final String accountId = "2000124699";


        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final List<PortfolioPosition> positions = List.of(
                TestData.createPortfolioPosition(ticker1),
                TestData.createPortfolioPosition(ticker2),
                TestData.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions(accountId)).thenReturn(positions);

        final PortfolioPosition position = extOperationsService.getSecurity(accountId, ticker2);

        Assertions.assertEquals(ticker2, position.ticker());
    }

    @Test
    void getPosition_returnsNull_whenNoPositionWithTicker() {
        final String accountId = "2000124699";

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";
        final String ticker3 = "ticker3";

        final List<PortfolioPosition> positions = List.of(
                TestData.createPortfolioPosition(ticker1),
                TestData.createPortfolioPosition(ticker2),
                TestData.createPortfolioPosition(ticker3)
        );
        Mockito.when(tinkoffService.getPortfolioPositions(accountId)).thenReturn(positions);

        final PortfolioPosition position = extOperationsService.getSecurity(accountId, "ticker");

        Assertions.assertNull(position);
    }

    // endregion

    // region getAvailableBalances tests

    @Test
    void getAvailableBalances_withBlockedValues() {
        // arrange
        final String accountId = "2000124699";

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = BigDecimal.valueOf(12.34);
        final BigDecimal blockedValue2 = BigDecimal.valueOf(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = BigDecimal.valueOf(1.2);
        final BigDecimal blockedGuaranteeValue2 = BigDecimal.valueOf(3.4);

        final MoneyValue blockedGuarantee1 = DataStructsHelper.createMoneyValue(currency1, blockedGuaranteeValue1);
        final MoneyValue blockedGuarantee2 = DataStructsHelper.createMoneyValue(currency2, blockedGuaranteeValue2);
        final List<MoneyValue> blockedGuarantee = List.of(blockedGuarantee1, blockedGuarantee2);

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(tinkoffService.getWithdrawLimits(accountId)).thenReturn(withdrawLimits);

        // action

        final List<Money> balances = extOperationsService.getAvailableBalances(accountId);

        // assert

        final Money money1 = TestData.createMoney(currency1, value1.subtract(blockedValue1).subtract(blockedGuaranteeValue1));
        final Money money2 = TestData.createMoney(currency2, value2.subtract(blockedValue2).subtract(blockedGuaranteeValue2));
        final List<Money> expectedBalances = List.of(money1, money2);

        AssertUtils.assertEquals(expectedBalances, balances);
    }

    @Test
    void getAvailableBalances_withoutBlockedValues() {
        // arrange
        final String accountId = "2000124699";

        final Currency currency1 = Currency.EUR;
        final Currency currency2 = Currency.USD;

        final BigDecimal value1 = BigDecimal.valueOf(123.456);
        final BigDecimal value2 = BigDecimal.valueOf(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);

        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);
        final List<MoneyValue> blocked = Collections.emptyList();
        final List<MoneyValue> blockedGuarantee = Collections.emptyList();

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(tinkoffService.getWithdrawLimits(accountId)).thenReturn(withdrawLimits);

        // action

        final List<Money> balances = extOperationsService.getAvailableBalances(accountId);

        // assert

        final Money money1 = TestData.createMoney(currency1, value1);
        final Money money2 = TestData.createMoney(currency2, value2);
        final List<Money> expectedBalances = List.of(money1, money2);

        AssertUtils.assertEquals(expectedBalances, balances);
    }

    // endregion

    // region getAvailableBalance tests

    @Test
    void getAvailableBalance_returnsBalance_whenNoBlocked() {
        final String accountId = "2000124699";

        final long rubBalance = 1000;

        final List<MoneyValue> moneys = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys);
        Mockito.when(tinkoffService.getWithdrawLimits(accountId)).thenReturn(withdrawLimits);

        final BigDecimal balance = extOperationsService.getAvailableBalance(accountId, Currency.RUB);

        AssertUtils.assertEquals(rubBalance, balance);
    }

    @Test
    void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists() {
        final String accountId = "2000124699";

        final long rubBalance = 1000;
        final long rubBlocked = 100;

        final List<MoneyValue> moneys = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final List<MoneyValue> blocked = List.of(TestData.createMoneyValue(rubBlocked, Currency.RUB));
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked);
        Mockito.when(tinkoffService.getWithdrawLimits(accountId)).thenReturn(withdrawLimits);

        final BigDecimal balance = extOperationsService.getAvailableBalance(accountId, Currency.RUB);

        AssertUtils.assertEquals(rubBalance - rubBlocked, balance);
    }

    @Test
    void getAvailableBalance_returnsBalanceMinusBlockedMinusBlockedGuarantee_whenCurrencyExists() {
        final String accountId = "2000124699";

        final long rubBalance = 1000;
        final long rubBlocked = 100;
        final long rubGuaranteeBlocked = 200;

        final List<MoneyValue> moneys = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final List<MoneyValue> blocked = List.of(TestData.createMoneyValue(rubBlocked, Currency.RUB));
        final List<MoneyValue> blockedGuarantee = List.of(TestData.createMoneyValue(rubGuaranteeBlocked, Currency.RUB));
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);
        Mockito.when(tinkoffService.getWithdrawLimits(accountId)).thenReturn(withdrawLimits);

        final BigDecimal balance = extOperationsService.getAvailableBalance(accountId, Currency.RUB);

        AssertUtils.assertEquals(rubBalance - rubBlocked - rubGuaranteeBlocked, balance);
    }

    @Test
    void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {
        final String accountId = "2000124699";

        final List<MoneyValue> moneys = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys);
        Mockito.when(tinkoffService.getWithdrawLimits(accountId)).thenReturn(withdrawLimits);

        final Executable executable = () -> extOperationsService.getAvailableBalance(accountId, Currency.RUB);
        Assertions.assertThrows(NoSuchElementException.class, executable, "No value present");
    }

    // endregion

}