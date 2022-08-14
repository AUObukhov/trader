package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service to get info about customer operations at market
 */
public interface ExtOperationsService {

    List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @Nullable final String ticker);

    List<PortfolioPosition> getPositions(final String accountId);

    WithdrawLimits getWithdrawLimits(String accountId);

    /**
     * @return position with given {@code ticker} at given {@code accountId} or null, if such position does not exist.
     * If {@code accountId} null, works with default broker account
     */
    default PortfolioPosition getSecurity(final String accountId, final String ticker) {
        final List<PortfolioPosition> allPositions = getPositions(accountId);
        return allPositions.stream()
                .filter(position -> ticker.equals(position.ticker()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return available balance of given {@code currency} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     * @throws NoSuchElementException if given {@code currency} not found.
     */
    default BigDecimal getAvailableBalance(final String accountId, final ru.obukhov.trader.market.model.Currency currency) {
        final WithdrawLimits withdrawLimits = getWithdrawLimits(accountId);
        final BigDecimal totalBalance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);
        return totalBalance.subtract(blockedBalance).subtract(blockedGuaranteeBalance);
    }

    /**
     * @return list of currencies balances at given {@code accountId}.
     */
    default List<Money> getAvailableBalances(final String accountId) {
        final WithdrawLimits withdrawLimits = getWithdrawLimits(accountId);
        final Map<Currency, BigDecimal> blocked = getBalanceMap(withdrawLimits.getBlocked());
        final Map<Currency, BigDecimal> blockedGuarantee = getBalanceMap(withdrawLimits.getBlockedGuarantee());
        return withdrawLimits.getMoney().stream()
                .map(money -> getAvailableMoney(money, blocked, blockedGuarantee))
                .toList();
    }

    private Map<Currency, BigDecimal> getBalanceMap(final List<Money> moneys) {
        return moneys.stream().collect(Collectors.toMap(Money::getCurrency, Money::getValue));
    }

    private Money getAvailableMoney(
            final Money money,
            final Map<Currency, BigDecimal> blocked,
            final Map<Currency, BigDecimal> blockedGuarantee
    ) {
        final Currency currency = money.getCurrency();
        final BigDecimal blockedValue = blocked.getOrDefault(currency, BigDecimal.ZERO);
        final BigDecimal blockedGuaranteeValue = blockedGuarantee.getOrDefault(currency, BigDecimal.ZERO);
        final BigDecimal value = money.getValue().subtract(blockedValue).subtract(blockedGuaranteeValue);
        return DataStructsHelper.createMoney(money.getCurrency(), value);
    }

}