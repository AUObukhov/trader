package ru.obukhov.trader.market.impl;

import lombok.RequiredArgsConstructor;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service to get information about customer portfolio
 */
@RequiredArgsConstructor
public class PortfolioService {

    private final TinkoffService tinkoffService;

    public List<PortfolioPosition> getPositions(final String accountId) {
        return tinkoffService.getPortfolioPositions(accountId);
    }

    /**
     * @return position with given {@code ticker} at given {@code accountId} or null, if such position does not exist.
     * If {@code accountId} null, works with default broker account
     */
    public PortfolioPosition getSecurity(final String accountId, final String ticker) {
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
    public BigDecimal getAvailableBalance(final String accountId, final ru.obukhov.trader.market.model.Currency currency) {
        final WithdrawLimits withdrawLimits = tinkoffService.getWithdrawLimits(accountId);
        final BigDecimal totalBalance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);
        return totalBalance.subtract(blockedBalance).subtract(blockedGuaranteeBalance);
    }

    /**
     * @return list of currencies balances at given {@code accountId}.
     */
    public List<Money> getAvailableBalances(final String accountId) {
        final WithdrawLimits withdrawLimits = tinkoffService.getWithdrawLimits(accountId);
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