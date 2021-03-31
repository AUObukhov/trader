package ru.obukhov.trader.bot.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;

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
        BigDecimal buyLotPrice = DecimalUtils.multiply(data.getAveragePositionPrice(), data.getLotSize());
        BigDecimal buyPricePlusCommission = DecimalUtils.addFraction(buyLotPrice, tradingProperties.getCommission());

        BigDecimal currentLotPrice = DecimalUtils.multiply(data.getCurrentPrice(), data.getLotSize());
        BigDecimal sellPriceMinusCommission = DecimalUtils.subtractFraction(
                currentLotPrice, tradingProperties.getCommission());

        double profit = DecimalUtils.getFractionDifference(sellPriceMinusCommission, buyPricePlusCommission)
                .doubleValue();

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
                .anyMatch(operation -> operation.getStatus() == OperationStatus.PROGRESS);
    }

    protected int getAvailableLots(DecisionData data) {
        BigDecimal currentLotPrice = DecimalUtils.multiply(data.getCurrentPrice(), data.getLotSize());
        BigDecimal currentLotPriceWithCommission =
                DecimalUtils.addFraction(currentLotPrice, tradingProperties.getCommission());
        return DecimalUtils.getIntegerQuotient(data.getBalance(), currentLotPriceWithCommission);
    }

}
