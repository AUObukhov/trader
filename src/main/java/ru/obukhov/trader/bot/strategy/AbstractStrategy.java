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
public abstract class AbstractStrategy implements Strategy {

    protected static final double MINIMUM_PROFIT = 0.01;

    @Getter
    protected final String name;
    protected final TradingProperties tradingProperties;

    protected AbstractStrategy(String name, TradingProperties tradingProperties) {
        this.name = name;
        this.tradingProperties = tradingProperties;
    }

    /**
     * @return decision to buy all available lots or decision to wait if no lots available
     */
    protected Decision getBuyOrWaitDecision(DecisionData data) {
        Decision decision;
        int availableLots = getAvailableLots(data);
        if (availableLots > 0) {
            decision = new Decision(DecisionAction.BUY, availableLots);
            log.debug("No position and current balance {} allows to buy {} lots. Decision is {}",
                    data.getBalance(), availableLots, decision.toPrettyString());
        } else {
            decision = Decision.WAIT_DECISION;
            log.debug("No position and current balance {} is not enough to buy any lots. Decision is {}",
                    data.getBalance(), decision.toPrettyString());
        }
        return decision;
    }

    /**
     * @return decision to sell all position if it is profitable with commission
     * or decision to wait otherwise
     */
    protected Decision getSellOrWaitDecision(DecisionData data) {
        double profit = getProfit(data);

        Decision decision;
        if (profit < MINIMUM_PROFIT) {
            decision = Decision.WAIT_DECISION;
            log.debug("Potential profit {} is lower than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        } else {
            decision = new Decision(DecisionAction.SELL, data.getPositionLotsCount());
            log.debug("Potential profit {} is greater than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        }

        return decision;
    }

    /**
     * @return possible average percent profit of selling all positions in given {@code DecisionData}
     */
    protected double getProfit(DecisionData data) {
        if (data.getPosition() == null) {
            log.debug("no position - no profit");
            return 0.0;
        }

        final BigDecimal averagePositionPrice = data.getAveragePositionPrice();
        BigDecimal buyPricePlusCommission =
                DecimalUtils.addFraction(averagePositionPrice, tradingProperties.getCommission());

        final BigDecimal currentPrice = data.getCurrentPrice();
        BigDecimal sellPriceMinusCommission =
                DecimalUtils.subtractFraction(currentPrice, tradingProperties.getCommission());

        double profit = DecimalUtils.getFractionDifference(sellPriceMinusCommission, buyPricePlusCommission)
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

    protected static boolean existsOperationInProgress(DecisionData data) {
        return data.getLastOperations().stream()
                .anyMatch(operation -> operation.getStatus() == OperationStatus.PROGRESS);
    }

    protected int getAvailableLots(DecisionData data) {
        BigDecimal currentLotPrice = DecimalUtils.multiply(data.getCurrentPrice(), data.getLotSize());
        BigDecimal currentLotPriceWithCommission =
                DecimalUtils.addFraction(currentLotPrice, tradingProperties.getCommission());
        return DecimalUtils.getIntegerQuotient(data.getBalance(), currentLotPriceWithCommission);
    }

}
