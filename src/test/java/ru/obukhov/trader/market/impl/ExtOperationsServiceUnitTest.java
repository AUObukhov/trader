package ru.obukhov.trader.market.impl;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
class ExtOperationsServiceUnitTest {

    // region getSecurity tests

    @Test
    void getSecurity() {
        final String accountId1 = "2000124699";
        final String accountId2 = "2000124698";

        final String ticker1 = "ticker1";
        final String ticker2 = "ticker2";

        final PortfolioPosition portfolioPosition1 = TestData.createPortfolioPosition(ticker1, 1);
        final PortfolioPosition portfolioPosition2 = TestData.createPortfolioPosition(ticker2, 2);
        final PortfolioPosition portfolioPosition3 = TestData.createPortfolioPosition(ticker1, 3);
        final PortfolioPosition portfolioPosition4 = TestData.createPortfolioPosition(ticker2, 4);

        final Map<String, List<PortfolioPosition>> accountsToPositions = Map.of(
                accountId1, List.of(portfolioPosition1, portfolioPosition2),
                accountId2, List.of(portfolioPosition3, portfolioPosition4)
        );

        final TestExtOperationsService extOperationsService = new TestExtOperationsService(accountsToPositions, Map.of());

        Assertions.assertEquals(portfolioPosition1, extOperationsService.getSecurity(accountId1, ticker1));
        Assertions.assertEquals(portfolioPosition2, extOperationsService.getSecurity(accountId1, ticker2));
        Assertions.assertEquals(portfolioPosition3, extOperationsService.getSecurity(accountId2, ticker1));
        Assertions.assertEquals(portfolioPosition4, extOperationsService.getSecurity(accountId2, ticker2));
    }

    // endregion

    // region getAvailableBalance tests

    @Test
    void getAvailableBalance_returnsBalance_whenNoBlocked() {
        final String accountId1 = "2000124699";
        final String accountId2 = "2000124698";

        final int usdBalance1 = 11;
        final int rubBalance1 = 12;
        final int eurBalance1 = 13;

        final int usdBalance2 = 21;
        final int rubBalance2 = 22;
        final int eurBalance2 = 23;

        final List<MoneyValue> moneys1 = List.of(
                TestData.createMoneyValue(usdBalance1, Currency.USD),
                TestData.createMoneyValue(rubBalance1, Currency.RUB),
                TestData.createMoneyValue(eurBalance1, Currency.EUR)
        );
        final List<MoneyValue> moneys2 = List.of(
                TestData.createMoneyValue(usdBalance2, Currency.USD),
                TestData.createMoneyValue(rubBalance2, Currency.RUB),
                TestData.createMoneyValue(eurBalance2, Currency.EUR)
        );

        final WithdrawLimits withdrawLimits1 = DataStructsHelper.createWithdrawLimits(moneys1);
        final WithdrawLimits withdrawLimits2 = DataStructsHelper.createWithdrawLimits(moneys2);

        final Map<String, WithdrawLimits> accountToWithdrawLimits = Map.of(
                accountId1, withdrawLimits1,
                accountId2, withdrawLimits2
        );
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), accountToWithdrawLimits);

        AssertUtils.assertEquals(usdBalance1, extOperationsService.getAvailableBalance(accountId1, Currency.USD));
        AssertUtils.assertEquals(rubBalance1, extOperationsService.getAvailableBalance(accountId1, Currency.RUB));
        AssertUtils.assertEquals(eurBalance1, extOperationsService.getAvailableBalance(accountId1, Currency.EUR));
        AssertUtils.assertEquals(usdBalance2, extOperationsService.getAvailableBalance(accountId2, Currency.USD));
        AssertUtils.assertEquals(rubBalance2, extOperationsService.getAvailableBalance(accountId2, Currency.RUB));
        AssertUtils.assertEquals(eurBalance2, extOperationsService.getAvailableBalance(accountId2, Currency.EUR));
    }

    @Test
    void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists() {
        final String accountId1 = "2000124699";
        final String accountId2 = "2000124698";

        final long rubBalance1 = 1000;
        final long rubBlocked1 = 100;

        final long rubBalance2 = 2000;
        final long rubBlocked2 = 200;

        final List<MoneyValue> moneys1 = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance1, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final List<MoneyValue> moneys2 = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance2, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );

        final List<MoneyValue> blocked1 = List.of(TestData.createMoneyValue(rubBlocked1, Currency.RUB));
        final List<MoneyValue> blocked2 = List.of(TestData.createMoneyValue(rubBlocked2, Currency.RUB));

        final WithdrawLimits withdrawLimits1 = DataStructsHelper.createWithdrawLimits(moneys1, blocked1);
        final WithdrawLimits withdrawLimits2 = DataStructsHelper.createWithdrawLimits(moneys2, blocked2);

        final Map<String, WithdrawLimits> accountToWithdrawLimits = Map.of(
                accountId1, withdrawLimits1,
                accountId2, withdrawLimits2
        );

        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), accountToWithdrawLimits);

        final BigDecimal balance1 = extOperationsService.getAvailableBalance(accountId1, Currency.RUB);
        final BigDecimal balance2 = extOperationsService.getAvailableBalance(accountId2, Currency.RUB);

        AssertUtils.assertEquals(rubBalance1 - rubBlocked1, balance1);
        AssertUtils.assertEquals(rubBalance2 - rubBlocked2, balance2);
    }

    @Test
    void getAvailableBalance_returnsBalanceMinusBlockedMinusBlockedGuarantee_whenCurrencyExists() {
        final String accountId1 = "2000124699";
        final String accountId2 = "2000124698";

        final long rubBalance1 = 1000;
        final long rubBlocked1 = 100;
        final long rubGuaranteeBlocked1 = 10;

        final long rubBalance2 = 2000;
        final long rubBlocked2 = 200;
        final long rubGuaranteeBlocked2 = 20;

        final List<MoneyValue> moneys1 = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance1, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final List<MoneyValue> moneys2 = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(rubBalance2, Currency.RUB),
                TestData.createMoneyValue(10, Currency.EUR)
        );

        final List<MoneyValue> blocked1 = List.of(TestData.createMoneyValue(rubBlocked1, Currency.RUB));
        final List<MoneyValue> blocked2 = List.of(TestData.createMoneyValue(rubBlocked2, Currency.RUB));

        final List<MoneyValue> blockedGuarantee1 = List.of(TestData.createMoneyValue(rubGuaranteeBlocked1, Currency.RUB));
        final List<MoneyValue> blockedGuarantee2 = List.of(TestData.createMoneyValue(rubGuaranteeBlocked2, Currency.RUB));

        final WithdrawLimits withdrawLimits1 = DataStructsHelper.createWithdrawLimits(moneys1, blocked1, blockedGuarantee1);
        final WithdrawLimits withdrawLimits2 = DataStructsHelper.createWithdrawLimits(moneys2, blocked2, blockedGuarantee2);

        final Map<String, WithdrawLimits> accountToWithdrawLimits = Map.of(
                accountId1, withdrawLimits1,
                accountId2, withdrawLimits2
        );
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), accountToWithdrawLimits);

        final BigDecimal balance1 = extOperationsService.getAvailableBalance(accountId1, Currency.RUB);
        final BigDecimal balance2 = extOperationsService.getAvailableBalance(accountId2, Currency.RUB);

        AssertUtils.assertEquals(rubBalance1 - rubBlocked1 - rubGuaranteeBlocked1, balance1);
        AssertUtils.assertEquals(rubBalance2 - rubBlocked2 - rubGuaranteeBlocked2, balance2);
    }

    @Test
    void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {
        final String accountId = "2000124699";

        final List<MoneyValue> moneys = List.of(
                TestData.createMoneyValue(100, Currency.USD),
                TestData.createMoneyValue(10, Currency.EUR)
        );
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys);
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), Map.of(accountId, withdrawLimits));

        final Executable executable = () -> extOperationsService.getAvailableBalance(accountId, Currency.RUB);
        Assertions.assertThrows(NoSuchElementException.class, executable, "No value present");
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
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), Map.of(accountId, withdrawLimits));

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
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), Map.of(accountId, withdrawLimits));

        // action

        final List<Money> balances = extOperationsService.getAvailableBalances(accountId);

        // assert

        final Money money1 = TestData.createMoney(currency1, value1);
        final Money money2 = TestData.createMoney(currency2, value2);
        final List<Money> expectedBalances = List.of(money1, money2);

        AssertUtils.assertEquals(expectedBalances, balances);
    }

    // endregion

    @AllArgsConstructor
    private static class TestExtOperationsService implements ExtOperationsService {
        private final Map<String, List<PortfolioPosition>> portfolioPositions;
        private final Map<String, WithdrawLimits> withdrawLimits;

        @Override
        public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String ticker) {
            return null;
        }

        @Override
        public List<PortfolioPosition> getPositions(final String accountId) {
            return portfolioPositions.get(accountId);
        }

        @Override
        public WithdrawLimits getWithdrawLimits(final String accountId) {
            return withdrawLimits.get(accountId);
        }
    }

}