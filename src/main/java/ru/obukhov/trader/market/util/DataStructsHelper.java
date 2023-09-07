package ru.obukhov.trader.market.util;

import lombok.experimental.UtilityClass;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.WithdrawLimitsResponse;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Utils to create and work with inconvenient Tinkoff data structures
 */
@UtilityClass
public class DataStructsHelper {

    public static MoneyValue newMoneyValue(final String currency, final BigDecimal value) {
        return MoneyValue.newBuilder()
                .setCurrency(currency)
                .setUnits(value.longValue())
                .setNano(DecimalUtils.getNano(value))
                .build();
    }

    public static Money newMoney(final BigDecimal value, final String currency) {
        return Money.builder()
                .currency(currency)
                .value(value)
                .build();
    }

    // region newWithdrawLimits

    public static WithdrawLimits newWithdrawLimits(final List<MoneyValue> moneys) {
        final List<MoneyValue> blocked = moneys.stream()
                .map(money -> newMoneyValue(money.getCurrency(), DecimalUtils.ZERO))
                .toList();
        return newWithdrawLimits(moneys, blocked, blocked);
    }

    public static WithdrawLimits newWithdrawLimits(final List<MoneyValue> moneys, final List<MoneyValue> blocked) {
        final List<MoneyValue> blockedGuarantee = moneys.stream()
                .map(money -> newMoneyValue(money.getCurrency(), DecimalUtils.ZERO))
                .toList();
        return newWithdrawLimits(moneys, blocked, blockedGuarantee);
    }

    public static WithdrawLimits newWithdrawLimits(
            final List<MoneyValue> moneys,
            final List<MoneyValue> blocked,
            final List<MoneyValue> blockedGuarantee
    ) {
        final WithdrawLimitsResponse withdrawLimitsResponse = WithdrawLimitsResponse.newBuilder()
                .addAllMoney(moneys)
                .addAllBlocked(blocked)
                .addAllBlockedGuarantee(blockedGuarantee)
                .build();
        return WithdrawLimits.fromResponse(withdrawLimitsResponse);
    }

    // endregion

    /**
     * @return balance value of first element from given list {@code moneys) where currency equals to given {@code currency}
     * @throws {@link NoSuchElementException} when there is no element with given {@code currency}
     */
    public static BigDecimal getBalance(final List<Money> moneys, final String currency) {
        return moneys.stream()
                .filter(money -> money.getCurrency().equals(currency))
                .findFirst()
                .map(Money::getValue)
                .orElseThrow();
    }

}