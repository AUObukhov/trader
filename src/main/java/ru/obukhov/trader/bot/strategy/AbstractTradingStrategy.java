package ru.obukhov.trader.bot.strategy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

import java.math.BigDecimal;

/**
 * Abstract strategy with some common methods
 */
@Slf4j
public abstract class AbstractTradingStrategy implements TradingStrategy {

    protected static final double MINIMUM_PROFIT = 0.01;
    protected static final int NAME_LENGTH_LIMIT = 32;

    @Getter
    protected final String name;
    protected final TradingProperties tradingProperties;

    protected AbstractTradingStrategy(final String name, final TradingProperties tradingProperties) {
        if (name.length() >= NAME_LENGTH_LIMIT) {
            throw new IllegalArgumentException("name must be shorter than " + NAME_LENGTH_LIMIT);
        }

        this.name = name;
        this.tradingProperties = tradingProperties;
    }

    /**
     * @return decision to buy all available lots or decision to wait if no lots available
     */
    protected Decision getBuyOrWaitDecision(final DecisionData data, StrategyCache strategyCache) {
        Decision decision;
        final int availableLots = getAvailableLots(data);
        if (availableLots > 0) {
            decision = new Decision(DecisionAction.BUY, availableLots, strategyCache);
            log.debug("No position and current balance {} allows to buy {} lots. Decision is {}",
                    data.getBalance(), availableLots, decision.toPrettyString());
        } else {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("No position and current balance {} is not enough to buy any lots. Decision is {}",
                    data.getBalance(), decision.toPrettyString());
        }
        return decision;
    }

    /**
     * @return decision to sell all position if it is profitable with commission
     * or decision to wait otherwise
     */
    protected Decision getSellOrWaitDecision(final DecisionData data, StrategyCache strategyCache) {
        final double profit = getProfit(data);

        Decision decision;
        if (profit < MINIMUM_PROFIT) {
            decision = new Decision(DecisionAction.WAIT, null, strategyCache);
            log.debug("Potential profit {} is lower than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        } else {
            decision = new Decision(DecisionAction.SELL, data.getPositionLotsCount(), strategyCache);
            log.debug("Potential profit {} is greater than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        }

        return decision;
    }

    /**
     * @return possible average percent profit of selling all positions in given {@code DecisionData}
     */
    protected double getProfit(final DecisionData data) {
        if (data.getPosition() == null) {
            log.debug("no position - no profit");
            return 0.0;
        }

        final BigDecimal averagePositionPrice = data.getAveragePositionPrice();
        final BigDecimal buyPricePlusCommission =
                DecimalUtils.addFraction(averagePositionPrice, tradingProperties.getCommission());

        final BigDecimal currentPrice = data.getCurrentPrice();
        final BigDecimal sellPriceMinusCommission =
                DecimalUtils.subtractFraction(currentPrice, tradingProperties.getCommission());

        final double profit = DecimalUtils.getFractionDifference(sellPriceMinusCommission, buyPricePlusCommission)
                .doubleValue();

        log.debug(
                "averagePositionPrice = {}, " +
                        "buyPricePlusCommission = {}, " +
                        "currentPrice = {}, " +
                        "sellPriceMinusCommission = {}, " +
                        "profit = {}, ",
                averagePositionPrice, buyPricePlusCommission, currentPrice, sellPriceMinusCommission, profit
        );

        return profit;
    }

    protected static boolean existsOperationInProgress(final DecisionData data) {
        return data.getLastOperations().stream()
                .anyMatch(operation -> operation.getStatus() == OperationStatus.PROGRESS);
    }

    protected int getAvailableLots(final DecisionData data) {
        final BigDecimal currentLotPrice = DecimalUtils.multiply(data.getCurrentPrice(), data.getLotSize());
        final BigDecimal currentLotPriceWithCommission =
                DecimalUtils.addFraction(currentLotPrice, tradingProperties.getCommission());
        return DecimalUtils.getIntegerQuotient(data.getBalance(), currentLotPriceWithCommission);
    }

}
