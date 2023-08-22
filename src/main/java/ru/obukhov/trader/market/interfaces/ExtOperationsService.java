package ru.obukhov.trader.market.interfaces;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.contract.v1.Quotation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Service to get info about customer operations at market
 */
public interface ExtOperationsService {

    List<Operation> getOperations(final String accountId, @NotNull final Interval interval, @NotNull final String figi);

    List<Position> getPositions(final String accountId);

    WithdrawLimits getWithdrawLimits(String accountId);

    /**
     * @return position with given {@code figi} at given {@code accountId} or null, if such position does not exist.
     * If {@code accountId} null, works with default broker account
     */
    default Position getSecurity(final String accountId, final String figi) {
        final List<Position> allPositions = getPositions(accountId);
        return allPositions.stream()
                .filter(position -> figi.equals(position.getFigi()))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return available balance of given {@code currency} at given {@code accountId}.
     * If {@code accountId} null, works with default broker account
     * @throws NoSuchElementException if given {@code currency} not found.
     */
    default Quotation getAvailableBalance(final String accountId, final String currency) {
        final WithdrawLimits withdrawLimits = getWithdrawLimits(accountId);
        final Quotation totalBalance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
        final Quotation blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
        final Quotation blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);
        return QuotationUtils.subtract(QuotationUtils.subtract(totalBalance, blockedBalance), blockedGuaranteeBalance);
    }

    /**
     * @return list of currencies balances at given {@code accountId}.
     */
    default List<Money> getAvailableBalances(final String accountId) {
        final WithdrawLimits withdrawLimits = getWithdrawLimits(accountId);
        final Map<String, BigDecimal> blocked = getBalanceMap(withdrawLimits.getBlocked());
        final Map<String, BigDecimal> blockedGuarantee = getBalanceMap(withdrawLimits.getBlockedGuarantee());
        return withdrawLimits.getMoney().stream()
                .map(money -> getAvailableMoney(money, blocked, blockedGuarantee))
                .toList();
    }

    private Map<String, BigDecimal> getBalanceMap(final List<Money> moneys) {
        return moneys.stream().collect(Collectors.toMap(Money::getCurrency, Money::getValue));
    }

    private Money getAvailableMoney(
            final Money money,
            final Map<String, BigDecimal> blocked,
            final Map<String, BigDecimal> blockedGuarantee
    ) {
        final String currency = money.getCurrency();
        final BigDecimal blockedValue = blocked.getOrDefault(currency, DecimalUtils.setDefaultScale(0));
        final BigDecimal blockedGuaranteeValue = blockedGuarantee.getOrDefault(currency, DecimalUtils.setDefaultScale(0));
        final BigDecimal value = money.getValue().subtract(blockedValue).subtract(blockedGuaranteeValue);
        return DataStructsHelper.createMoney(value, money.getCurrency());
    }

}