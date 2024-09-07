package ru.obukhov.trader.trading.strategy.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.trading.model.TradingStrategyParams;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.tinkoff.piapi.contract.v1.OperationState;

import java.math.BigDecimal;
import java.util.Collection;

@Slf4j
public abstract class AbstractTradingStrategy implements TradingStrategy {

    @Getter
    protected final String name;
    protected final TradingStrategyParams params;

    protected AbstractTradingStrategy(final String name, final TradingStrategyParams params) {
        this.name = params == null ? name : name + " " + params;
        this.params = params;
    }

    protected Decision getBuyOrWaitDecision(
            final DecisionData data,
            final long lotsQuantity
    ) {
        Assert.isTrue(lotsQuantity > 0, "lotsQuantity must be above 0. Got " + lotsQuantity);

        final Decision decision = data.getAvailableLots() >= lotsQuantity
                ? new Decision(DecisionAction.BUY, lotsQuantity)
                : new Decision(DecisionAction.WAIT, null);

        log.debug("Available lots - {}. Requested lots - {}. Decision is {}", data.getAvailableLots(), lotsQuantity, decision.toPrettyString());

        return decision;
    }

    protected Decision getSellOrWaitDecision(
            final DecisionData data,
            final BigDecimal currentPrice,
            final BigDecimal commission,
            final Float minimumProfit
    ) {
        Decision decision;

        if (minimumProfit < 0) {
            decision = new Decision(DecisionAction.WAIT, null);
            log.debug("Minimum profit {} is negative. Decision is {}", minimumProfit, decision.toPrettyString());
        } else {
            final double profit = getProfit(data, currentPrice, commission);
            if (profit < minimumProfit) {
                decision = new Decision(DecisionAction.WAIT, null);
                log.debug("Potential profit {} is lower than minimum profit {}. Decision is {}", profit, minimumProfit, decision.toPrettyString());
            } else {
                decision = new Decision(DecisionAction.SELL, data.getQuantity());
                log.debug("Potential profit {} is greater than minimum profit {}. Decision is {}", profit, minimumProfit, decision.toPrettyString());
            }
        }

        return decision;
    }

    private double getProfit(final DecisionData data, final BigDecimal currentPrice, final BigDecimal commission) {
        if (data.getPosition() == null) {
            log.debug("no position - no profit");
            return 0.0;
        }

        final BigDecimal sellPriceMinusCommission = DecimalUtils.subtractFraction(currentPrice, commission);
        final BigDecimal averagePositionPrice = DecimalUtils.setDefaultScale(data.getAveragePositionPrice());
        final BigDecimal buyPricePlusCommission = DecimalUtils.addFraction(averagePositionPrice, commission);
        final double profit = DecimalUtils.getFractionDifference(sellPriceMinusCommission, buyPricePlusCommission).doubleValue();

        final String format = "averagePositionPrice = {}, buyPricePlusCommission = {}, currentPrice = {}, sellPriceMinusCommission = {}, profit = {}";
        log.debug(format, averagePositionPrice, buyPricePlusCommission, currentPrice, sellPriceMinusCommission, profit);

        return profit;
    }

    protected static boolean existsOperationStateIsUnspecified(final DecisionsData data) {
        return data.getDecisionDatas().stream()
                .map(DecisionData::getLastOperations)
                .flatMap(Collection::stream)
                .anyMatch(operation -> operation.getState() == OperationState.OPERATION_STATE_UNSPECIFIED);
    }

}
