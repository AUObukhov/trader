package ru.obukhov.trader.market.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.obukhov.trader.common.util.TradingDayUtils;
import ru.obukhov.trader.market.interfaces.Context;
import ru.obukhov.trader.market.model.FakeBalance;
import ru.obukhov.trader.market.model.FakePortfolio;
import ru.obukhov.trader.market.model.TradingDay;
import ru.tinkoff.piapi.contract.v1.Operation;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Prices are loaded from real market, but any operations do not affect the real portfolio - all data is stored in memory.
 */
@Slf4j
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FakeContext implements Context {

    @Getter
    @Setter
    private OffsetDateTime currentDateTime;

    private final List<FakePortfolio> portfolios;

    public FakeContext(final OffsetDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
        this.portfolios = new ArrayList<>();
    }

    public FakeContext(final String accountId, final OffsetDateTime currentDateTime, final Map<String, BigDecimal> initialBalances) {
        this(currentDateTime);

        addInvestments(accountId, currentDateTime, initialBalances);
    }

    /**
     * Changes {@code currentTimestamp} to:<br/>
     * - {@code nextMinute} if it is inside any tradingDays<br/>
     * - the first minute of first tradingDays after {@code nextMinute} if it is not inside any of tradingDays<br/>
     * - null if all tradingDays are before {@code nextMinute}
     *
     * @param tradingSchedule list of trading days, items must not have intersections, must be sorted is ascending order
     * @return {@code currentTimestamp}
     * @Terms: nextMinute – {@code currentTimestamp} + 1 minute<br/>
     * tradingDay – item of {@code tradingSchedule} with {@code isTradingDay=true}<br/>
     */
    public OffsetDateTime nextScheduleMinute(final List<TradingDay> tradingSchedule) {
        currentDateTime = TradingDayUtils.nextScheduleMinute(tradingSchedule, currentDateTime);
        return currentDateTime;
    }

    // region balance

    /**
     * sets given {@code amount} as balance of given {@code currency} and at given {@code accountId}
     */
    public void setBalance(final String accountId, final String currency, final BigDecimal amount) {
        computeIfAbsentBalance(accountId, currency).setCurrentAmount(amount);
    }

    /**
     * @return balance of given {@code currency} and at given {@code accountId}
     */
    public BigDecimal getBalance(final String accountId, final String currency) {
        return computeIfAbsentBalance(accountId, currency).getCurrentAmount();
    }

    /**
     * @return balances of all currencies at given {@code accountId}
     */
    public Map<String, BigDecimal> getBalances(final String accountId) {
        return computeIfAbsentPortfolio(accountId).getBalances()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getCurrentAmount()));
    }

    // endregion

    // region investments

    /**
     * Adds given {@code amount} to balance of given {@code currency} and record to history of investments with given {@code dateTime}
     */
    public void addInvestment(
            final String accountId,
            final OffsetDateTime dateTime,
            final String currency,
            final BigDecimal amount
    ) {
        computeIfAbsentBalance(accountId, currency).addInvestment(dateTime, amount);
    }

    /**
     * Adds all given {@code investments} to balances and record to history of investments with given {@code dateTime}.<br/>
     * Keys from investments - currencies, values - money amounts
     */
    public void addInvestments(final String accountId, final OffsetDateTime dateTime, final Map<String, BigDecimal> investments) {
        for (final Map.Entry<String, BigDecimal> entry : investments.entrySet()) {
            addInvestment(accountId, dateTime, entry.getKey(), entry.getValue());
        }
    }

    /**
     * @return all investments of given {@code currency} and at given {@code accountId} by timestamp in ascending order
     */
    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(final String accountId, final String currency) {
        return computeIfAbsentBalance(accountId, currency).getInvestments();
    }

    // endregion

    // region operations

    public void addOperation(final String accountId, final Operation operation) {
        computeIfAbsentPortfolio(accountId).getOperations().add(operation);
    }

    public Set<Operation> getOperations(final String accountId) {
        return new HashSet<>(computeIfAbsentPortfolio(accountId).getOperations());
    }

    // endregion

    // region positions

    public void addPosition(final String accountId, final String figi, final Position position) {
        computeIfAbsentPortfolio(accountId).getFigiesToPositions().put(figi, position);
    }

    public Position getPosition(final String accountId, final String figi) {
        return computeIfAbsentPortfolio(accountId).getFigiesToPositions().get(figi);
    }

    public List<Position> getPositions(final String accountId) {
        return new ArrayList<>(computeIfAbsentPortfolio(accountId).getFigiesToPositions().values());
    }

    public void removePosition(final String accountId, final String figi) {
        computeIfAbsentPortfolio(accountId).getFigiesToPositions().remove(figi);
    }

    // endregion

    private FakeBalance computeIfAbsentBalance(final String accountId, final String currency) {
        return computeIfAbsentPortfolio(accountId).getBalances().computeIfAbsent(currency, currencyKey -> new FakeBalance());
    }

    private FakePortfolio computeIfAbsentPortfolio(final String accountId) {
        final Optional<FakePortfolio> desiredPortfolio = portfolios.stream()
                .filter(portfolio -> accountId.equals(portfolio.getAccountId()))
                .findFirst();
        if (desiredPortfolio.isPresent()) {
            return desiredPortfolio.get();
        } else {
            final FakePortfolio portfolio = new FakePortfolio(accountId);
            portfolios.add(portfolio);
            return portfolio;
        }
    }

}