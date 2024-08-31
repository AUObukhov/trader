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

    public OffsetDateTime nextScheduleMinute(final List<TradingDay> tradingSchedule) {
        currentDateTime = TradingDayUtils.nextScheduleMinute(tradingSchedule, currentDateTime);
        return currentDateTime;
    }

    // region balance

    public void setBalance(final String accountId, final String currency, final BigDecimal amount) {
        computeIfAbsentBalance(accountId, currency).setCurrentAmount(amount);
    }

    public BigDecimal getBalance(final String accountId, final String currency) {
        return computeIfAbsentBalance(accountId, currency).getCurrentAmount();
    }

    public Map<String, BigDecimal> getBalances(final String accountId) {
        return computeIfAbsentPortfolio(accountId).getBalances()
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getCurrentAmount()));
    }

    // endregion

    // region investments

    public void addInvestment(
            final String accountId,
            final OffsetDateTime dateTime,
            final String currency,
            final BigDecimal amount
    ) {
        computeIfAbsentBalance(accountId, currency).addInvestment(dateTime, amount);
    }

    public void addInvestments(final String accountId, final OffsetDateTime dateTime, final Map<String, BigDecimal> investments) {
        for (final Map.Entry<String, BigDecimal> entry : investments.entrySet()) {
            addInvestment(accountId, dateTime, entry.getKey(), entry.getValue());
        }
    }

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