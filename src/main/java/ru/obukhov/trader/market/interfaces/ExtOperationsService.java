package ru.obukhov.trader.market.interfaces;

import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;
import ru.tinkoff.piapi.core.models.WithdrawLimits;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ExtOperationsService {

    List<Operation> getOperations(final String accountId, final Interval interval, final String figi);

    List<Position> getPositions(final String accountId);

    WithdrawLimits getWithdrawLimits(String accountId);

    default Position getSecurity(final String accountId, final String figi) {
        final List<Position> allPositions = getPositions(accountId);
        return allPositions.stream()
                .filter(position -> figi.equals(position.getFigi()))
                .findFirst()
                .orElse(null);
    }

    default BigDecimal getAvailableBalance(final String accountId, final String currency) {
        final WithdrawLimits withdrawLimits = getWithdrawLimits(accountId);
        final BigDecimal totalBalance = DataStructsHelper.getBalance(withdrawLimits.getMoney(), currency);
        final BigDecimal blockedBalance = DataStructsHelper.getBalance(withdrawLimits.getBlocked(), currency);
        final BigDecimal blockedGuaranteeBalance = DataStructsHelper.getBalance(withdrawLimits.getBlockedGuarantee(), currency);
        return totalBalance.subtract(blockedBalance).subtract(blockedGuaranteeBalance);
    }

    default Map<String, BigDecimal> getAvailableBalances(final String accountId) {
        final WithdrawLimits withdrawLimits = getWithdrawLimits(accountId);
        final Map<String, BigDecimal> blocked = getBalanceMap(withdrawLimits.getBlocked());
        final Map<String, BigDecimal> blockedGuarantee = getBalanceMap(withdrawLimits.getBlockedGuarantee());
        return withdrawLimits.getMoney().stream()
                .collect(Collectors.toMap(Money::getCurrency, money -> getAvailableBalance(money, blocked, blockedGuarantee)));
    }

    private Map<String, BigDecimal> getBalanceMap(final List<Money> moneys) {
        return moneys.stream().collect(Collectors.toMap(Money::getCurrency, Money::getValue));
    }

    private BigDecimal getAvailableBalance(
            final Money money,
            final Map<String, BigDecimal> blocked,
            final Map<String, BigDecimal> blockedGuarantee
    ) {
        final String currency = money.getCurrency();
        final BigDecimal blockedValue = blocked.getOrDefault(currency, DecimalUtils.ZERO);
        final BigDecimal blockedGuaranteeValue = blockedGuarantee.getOrDefault(currency, DecimalUtils.ZERO);
        return money.getValue().subtract(blockedValue).subtract(blockedGuaranteeValue);
    }

}