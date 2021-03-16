package ru.obukhov.trader.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.interfaces.Strategy;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;

import java.math.BigDecimal;

/**
 * Abstract strategy with some common methods
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractStrategy implements Strategy {

    protected static final double MINIMUM_PROFIT = 0.01;

    protected final TradingProperties tradingProperties;

    /**
     * @return possible average percent profit of selling all positions in given {@code DecisionData}
     */
    protected double getProfit(DecisionData data) {
        BigDecimal buyLotPrice = MathUtils.multiply(data.getAveragePositionPrice(), data.getLotSize());
        BigDecimal buyPricePlusCommission = MathUtils.addFraction(buyLotPrice, tradingProperties.getCommission());

        BigDecimal currentLotPrice = MathUtils.multiply(data.getCurrentPrice(), data.getLotSize());
        BigDecimal sellPriceMinusCommission = MathUtils.subtractFraction(
                currentLotPrice, tradingProperties.getCommission());

        double profit = MathUtils.getFractionDifference(sellPriceMinusCommission, buyPricePlusCommission).doubleValue();

        log.debug("buyLotPrice = {}, "
                        + "buyPricePlusCommission = {}, "
                        + "currentLotPrice = {}, "
                        + "sellPriceMinusCommission = {}, "
                        + "profit = {}, ",
                buyLotPrice, buyPricePlusCommission, currentLotPrice, sellPriceMinusCommission, profit);

        return profit;
    }

    protected static boolean existsOperationInProgress(DecisionData data) {
        return data.getLastOperations().stream()
                .anyMatch(operation -> operation.status == OperationStatus.Progress);
    }

    protected int getAvailableLots(DecisionData data) {
        BigDecimal currentLotPrice = MathUtils.multiply(data.getCurrentPrice(), data.getLotSize());
        BigDecimal currentLotPriceWithCommission =
                MathUtils.addFraction(currentLotPrice, tradingProperties.getCommission());
        return MathUtils.getIntegerQuotient(data.getBalance(), currentLotPriceWithCommission);
    }

}
