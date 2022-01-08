package ru.obukhov.trader.market.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import ru.obukhov.trader.trading.model.BackTestOperation;
import ru.tinkoff.invest.openapi.model.rest.Currency;

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
import java.util.stream.Stream;

/**
 * Class, containing in memory data about back tested portfolio and current market dateTime
 */
public class FakeContext {

    @Getter
    @Setter
    private OffsetDateTime currentDateTime;

    private final List<FakePortfolio> portfolios;

    public FakeContext(final OffsetDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
        this.portfolios = new ArrayList<>();
    }

    public FakeContext(
            final OffsetDateTime currentDateTime,
            @Nullable final String brokerAccountId,
            final Currency currency,
            final BigDecimal initialBalance
    ) {
        this(currentDateTime);

        addInvestment(brokerAccountId, currency, initialBalance);
    }

    /**
     * Adds given {@code amount} to balance of given {@code currency} and record to history of investments with current dateTime
     */
    public void addInvestment(@Nullable final String brokerAccountId, final Currency currency, final BigDecimal amount) {
        computeIfAbsentBalance(brokerAccountId, currency).addInvestment(currentDateTime, amount);
    }

    /**
     * Adds given {@code amount} to balance of given {@code currency} and record to history of investments with given {@code dateTime}
     */
    public void addInvestment(
            @Nullable final String brokerAccountId,
            final OffsetDateTime dateTime,
            final Currency currency,
            final BigDecimal amount
    ) {
        computeIfAbsentBalance(brokerAccountId, currency).addInvestment(dateTime, amount);
    }

    public BigDecimal getBalance(@Nullable final String brokerAccountId, final Currency currency) {
        return computeIfAbsentBalance(brokerAccountId, currency).getCurrentAmount();
    }

    public Map<Currency, BigDecimal> getBalances(@Nullable final String brokerAccountId) {
        return Stream.of(Currency.values()).collect(Collectors.toMap(currency -> currency, currency -> getBalance(brokerAccountId, currency)));
    }

    public SortedMap<OffsetDateTime, BigDecimal> getInvestments(@Nullable final String brokerAccountId, final Currency currency) {
        return computeIfAbsentBalance(brokerAccountId, currency).getInvestments();
    }

    public void setCurrentBalance(@Nullable final String brokerAccountId, final Currency currency, final BigDecimal amount) {
        computeIfAbsentBalance(brokerAccountId, currency).setCurrentAmount(amount);
    }

    public void addOperation(@Nullable final String brokerAccountId, final BackTestOperation operation) {
        computeIfAbsentPortfolio(brokerAccountId).getOperations().add(operation);
    }

    public Set<BackTestOperation> getOperations(@Nullable final String brokerAccountId) {
        return new HashSet<>(computeIfAbsentPortfolio(brokerAccountId).getOperations());
    }

    public void addPosition(@Nullable final String brokerAccountId, final String ticker, PortfolioPosition position) {
        computeIfAbsentPortfolio(brokerAccountId).getTickersToPositions().put(ticker, position);
    }

    public void removePosition(@Nullable final String brokerAccountId, final String ticker) {
        computeIfAbsentPortfolio(brokerAccountId).getTickersToPositions().remove(ticker);
    }

    public PortfolioPosition getPosition(@Nullable final String brokerAccountId, final String ticker) {
        return computeIfAbsentPortfolio(brokerAccountId).getTickersToPositions().get(ticker);
    }

    public List<PortfolioPosition> getPositions(@Nullable final String brokerAccountId) {
        return new ArrayList<>(computeIfAbsentPortfolio(brokerAccountId).getTickersToPositions().values());
    }

    private FakeBalance computeIfAbsentBalance(@Nullable final String brokerAccountId, final Currency currency) {
        return computeIfAbsentPortfolio(brokerAccountId).getBalances().computeIfAbsent(currency, currencyKey -> new FakeBalance());
    }

    private FakePortfolio computeIfAbsentPortfolio(@Nullable final String brokerAccountId) {
        if (brokerAccountId == null) {
            if (portfolios.isEmpty()) {
                portfolios.add(new FakePortfolio(null));
            }
            return portfolios.get(0);
        } else {
            final Optional<FakePortfolio> desiredPortfolio = portfolios.stream()
                    .filter(portfolio -> brokerAccountId.equals(portfolio.getBrokerAccountId()))
                    .findFirst();
            if (desiredPortfolio.isPresent()) {
                return desiredPortfolio.get();
            } else {
                final FakePortfolio portfolio = new FakePortfolio(brokerAccountId);
                portfolios.add(portfolio);
                return portfolio;
            }
        }
    }

}