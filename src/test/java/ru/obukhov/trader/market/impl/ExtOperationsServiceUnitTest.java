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
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;
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
        final String accountId1 = TestData.ACCOUNT_ID1;
        final String accountId2 = TestData.ACCOUNT_ID2;

        final String figi1 = TestShare1.FIGI;
        final String figi2 = TestShare2.FIGI;

        final Position portfolioPosition1 = Position.builder().figi(figi1).quantityLots(BigDecimal.valueOf(1)).build();
        final Position portfolioPosition2 = Position.builder().figi(figi2).quantityLots(BigDecimal.valueOf(2)).build();
        final Position portfolioPosition3 = Position.builder().figi(figi1).quantityLots(BigDecimal.valueOf(3)).build();
        final Position portfolioPosition4 = Position.builder().figi(figi2).quantityLots(BigDecimal.valueOf(4)).build();

        final Map<String, List<Position>> accountsToPositions = Map.of(
                accountId1, List.of(portfolioPosition1, portfolioPosition2),
                accountId2, List.of(portfolioPosition3, portfolioPosition4)
        );

        final TestExtOperationsService extOperationsService = new TestExtOperationsService(accountsToPositions, Map.of());

        Assertions.assertEquals(portfolioPosition1, extOperationsService.getSecurity(accountId1, figi1));
        Assertions.assertEquals(portfolioPosition2, extOperationsService.getSecurity(accountId1, figi2));
        Assertions.assertEquals(portfolioPosition3, extOperationsService.getSecurity(accountId2, figi1));
        Assertions.assertEquals(portfolioPosition4, extOperationsService.getSecurity(accountId2, figi2));
    }

    // endregion

    // region getAvailableBalance tests

    @Test
    void getAvailableBalance_returnsBalance_whenNoBlocked() {
        final String accountId1 = TestData.ACCOUNT_ID1;
        final String accountId2 = TestData.ACCOUNT_ID2;

        final int usdBalance1 = 11;
        final int rubBalance1 = 12;
        final int eurBalance1 = 13;

        final int usdBalance2 = 21;
        final int rubBalance2 = 22;
        final int eurBalance2 = 23;

        final List<MoneyValue> moneys1 = List.of(
                TestData.createTinkoffMoneyValue(usdBalance1, Currencies.USD),
                TestData.createTinkoffMoneyValue(rubBalance1, Currencies.RUB),
                TestData.createTinkoffMoneyValue(eurBalance1, Currencies.EUR)
        );
        final List<MoneyValue> moneys2 = List.of(
                TestData.createTinkoffMoneyValue(usdBalance2, Currencies.USD),
                TestData.createTinkoffMoneyValue(rubBalance2, Currencies.RUB),
                TestData.createTinkoffMoneyValue(eurBalance2, Currencies.EUR)
        );

        final WithdrawLimits withdrawLimits1 = DataStructsHelper.createWithdrawLimits(moneys1);
        final WithdrawLimits withdrawLimits2 = DataStructsHelper.createWithdrawLimits(moneys2);

        final Map<String, WithdrawLimits> accountToWithdrawLimits = Map.of(
                accountId1, withdrawLimits1,
                accountId2, withdrawLimits2
        );
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), accountToWithdrawLimits);

        AssertUtils.assertEquals(usdBalance1, extOperationsService.getAvailableBalance(accountId1, Currencies.USD));
        AssertUtils.assertEquals(rubBalance1, extOperationsService.getAvailableBalance(accountId1, Currencies.RUB));
        AssertUtils.assertEquals(eurBalance1, extOperationsService.getAvailableBalance(accountId1, Currencies.EUR));
        AssertUtils.assertEquals(usdBalance2, extOperationsService.getAvailableBalance(accountId2, Currencies.USD));
        AssertUtils.assertEquals(rubBalance2, extOperationsService.getAvailableBalance(accountId2, Currencies.RUB));
        AssertUtils.assertEquals(eurBalance2, extOperationsService.getAvailableBalance(accountId2, Currencies.EUR));
    }

    @Test
    void getAvailableBalance_returnsBalanceMinusBlocked_whenCurrencyExists() {
        final String accountId1 = TestData.ACCOUNT_ID1;
        final String accountId2 = TestData.ACCOUNT_ID2;

        final long rubBalance1 = 1000;
        final long rubBlocked1 = 100;

        final long rubBalance2 = 2000;
        final long rubBlocked2 = 200;

        final List<MoneyValue> moneys1 = List.of(
                TestData.createTinkoffMoneyValue(100, Currencies.USD),
                TestData.createTinkoffMoneyValue(rubBalance1, Currencies.RUB),
                TestData.createTinkoffMoneyValue(10, Currencies.EUR)
        );
        final List<MoneyValue> moneys2 = List.of(
                TestData.createTinkoffMoneyValue(100, Currencies.USD),
                TestData.createTinkoffMoneyValue(rubBalance2, Currencies.RUB),
                TestData.createTinkoffMoneyValue(10, Currencies.EUR)
        );

        final List<MoneyValue> blocked1 = List.of(TestData.createTinkoffMoneyValue(rubBlocked1, Currencies.RUB));
        final List<MoneyValue> blocked2 = List.of(TestData.createTinkoffMoneyValue(rubBlocked2, Currencies.RUB));

        final WithdrawLimits withdrawLimits1 = DataStructsHelper.createWithdrawLimits(moneys1, blocked1);
        final WithdrawLimits withdrawLimits2 = DataStructsHelper.createWithdrawLimits(moneys2, blocked2);

        final Map<String, WithdrawLimits> accountToWithdrawLimits = Map.of(
                accountId1, withdrawLimits1,
                accountId2, withdrawLimits2
        );

        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), accountToWithdrawLimits);

        final BigDecimal balance1 = extOperationsService.getAvailableBalance(accountId1, Currencies.RUB);
        final BigDecimal balance2 = extOperationsService.getAvailableBalance(accountId2, Currencies.RUB);

        AssertUtils.assertEquals(rubBalance1 - rubBlocked1, balance1);
        AssertUtils.assertEquals(rubBalance2 - rubBlocked2, balance2);
    }

    @Test
    void getAvailableBalance_returnsBalanceMinusBlockedMinusBlockedGuarantee_whenCurrencyExists() {
        final String accountId1 = TestData.ACCOUNT_ID1;
        final String accountId2 = TestData.ACCOUNT_ID2;

        final long rubBalance1 = 1000;
        final long rubBlocked1 = 100;
        final long rubGuaranteeBlocked1 = 10;

        final long rubBalance2 = 2000;
        final long rubBlocked2 = 200;
        final long rubGuaranteeBlocked2 = 20;

        final List<MoneyValue> moneys1 = List.of(
                TestData.createTinkoffMoneyValue(100, Currencies.USD),
                TestData.createTinkoffMoneyValue(rubBalance1, Currencies.RUB),
                TestData.createTinkoffMoneyValue(10, Currencies.EUR)
        );
        final List<MoneyValue> moneys2 = List.of(
                TestData.createTinkoffMoneyValue(100, Currencies.USD),
                TestData.createTinkoffMoneyValue(rubBalance2, Currencies.RUB),
                TestData.createTinkoffMoneyValue(10, Currencies.EUR)
        );

        final List<MoneyValue> blocked1 = List.of(TestData.createTinkoffMoneyValue(rubBlocked1, Currencies.RUB));
        final List<MoneyValue> blocked2 = List.of(TestData.createTinkoffMoneyValue(rubBlocked2, Currencies.RUB));

        final List<MoneyValue> blockedGuarantee1 = List.of(TestData.createTinkoffMoneyValue(rubGuaranteeBlocked1, Currencies.RUB));
        final List<MoneyValue> blockedGuarantee2 = List.of(TestData.createTinkoffMoneyValue(rubGuaranteeBlocked2, Currencies.RUB));

        final WithdrawLimits withdrawLimits1 = DataStructsHelper.createWithdrawLimits(moneys1, blocked1, blockedGuarantee1);
        final WithdrawLimits withdrawLimits2 = DataStructsHelper.createWithdrawLimits(moneys2, blocked2, blockedGuarantee2);

        final Map<String, WithdrawLimits> accountToWithdrawLimits = Map.of(
                accountId1, withdrawLimits1,
                accountId2, withdrawLimits2
        );
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), accountToWithdrawLimits);

        final BigDecimal balance1 = extOperationsService.getAvailableBalance(accountId1, Currencies.RUB);
        final BigDecimal balance2 = extOperationsService.getAvailableBalance(accountId2, Currencies.RUB);

        AssertUtils.assertEquals(rubBalance1 - rubBlocked1 - rubGuaranteeBlocked1, balance1);
        AssertUtils.assertEquals(rubBalance2 - rubBlocked2 - rubGuaranteeBlocked2, balance2);
    }

    @Test
    void getAvailableBalance_throwsNoSuchElementException_whenNoCurrency() {
        final String accountId = TestData.ACCOUNT_ID1;

        final List<MoneyValue> moneys = List.of(
                TestData.createTinkoffMoneyValue(100, Currencies.USD),
                TestData.createTinkoffMoneyValue(10, Currencies.EUR)
        );
        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys);
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), Map.of(accountId, withdrawLimits));

        final Executable executable = () -> extOperationsService.getAvailableBalance(accountId, Currencies.RUB);
        AssertUtils.assertThrowsWithMessage(NoSuchElementException.class, executable, "No value present");
    }

    // endregion

    // region getAvailableBalances tests

    @Test
    void getAvailableBalances_withBlockedValues() {
        // arrange
        final String accountId = TestData.ACCOUNT_ID1;

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

        final MoneyValue moneyValue1 = DataStructsHelper.createMoneyValue(currency1, value1);
        final MoneyValue moneyValue2 = DataStructsHelper.createMoneyValue(currency2, value2);
        final List<MoneyValue> moneys = List.of(moneyValue1, moneyValue2);

        final BigDecimal blockedValue1 = DecimalUtils.setDefaultScale(12.34);
        final BigDecimal blockedValue2 = DecimalUtils.setDefaultScale(56.78);

        final MoneyValue blocked1 = DataStructsHelper.createMoneyValue(currency1, blockedValue1);
        final MoneyValue blocked2 = DataStructsHelper.createMoneyValue(currency2, blockedValue2);
        final List<MoneyValue> blocked = List.of(blocked1, blocked2);

        final BigDecimal blockedGuaranteeValue1 = DecimalUtils.setDefaultScale(1.2);
        final BigDecimal blockedGuaranteeValue2 = DecimalUtils.setDefaultScale(3.4);

        final MoneyValue blockedGuarantee1 = DataStructsHelper.createMoneyValue(currency1, blockedGuaranteeValue1);
        final MoneyValue blockedGuarantee2 = DataStructsHelper.createMoneyValue(currency2, blockedGuaranteeValue2);
        final List<MoneyValue> blockedGuarantee = List.of(blockedGuarantee1, blockedGuarantee2);

        final WithdrawLimits withdrawLimits = DataStructsHelper.createWithdrawLimits(moneys, blocked, blockedGuarantee);
        final TestExtOperationsService extOperationsService = new TestExtOperationsService(Map.of(), Map.of(accountId, withdrawLimits));

        // action

        final List<Money> balances = extOperationsService.getAvailableBalances(accountId);

        // assert

        final Money money1 = DataStructsHelper.createMoney(currency1, value1.subtract(blockedValue1).subtract(blockedGuaranteeValue1));
        final Money money2 = DataStructsHelper.createMoney(currency2, value2.subtract(blockedValue2).subtract(blockedGuaranteeValue2));
        final List<Money> expectedBalances = List.of(money1, money2);

        AssertUtils.assertEquals(expectedBalances, balances);
    }

    @Test
    void getAvailableBalances_withoutBlockedValues() {
        // arrange
        final String accountId = TestData.ACCOUNT_ID1;

        final String currency1 = Currencies.EUR;
        final String currency2 = Currencies.USD;

        final BigDecimal value1 = DecimalUtils.setDefaultScale(123.456);
        final BigDecimal value2 = DecimalUtils.setDefaultScale(789.012);

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

        final Money money1 = DataStructsHelper.createMoney(currency1, value1);
        final Money money2 = DataStructsHelper.createMoney(currency2, value2);
        final List<Money> expectedBalances = List.of(money1, money2);

        AssertUtils.assertEquals(expectedBalances, balances);
    }

    // endregion

    @AllArgsConstructor
    private static class TestExtOperationsService implements ExtOperationsService {
        private final Map<String, List<Position>> portfolioPositions;
        private final Map<String, WithdrawLimits> withdrawLimits;

        @Override
        public List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String figi) {
            return null;
        }

        @Override
        public List<Position> getPositions(final String accountId) {
            return portfolioPositions.get(accountId);
        }

        @Override
        public WithdrawLimits getWithdrawLimits(final String accountId) {
            return withdrawLimits.get(accountId);
        }
    }

}