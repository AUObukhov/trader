package ru.obukhov.investor.bot.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;

import java.math.BigDecimal;

/**
 * Abstract decider with some common methods
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDecider implements Decider {

    protected static final double MINIMUM_PROFIT = 0.01;

    protected final TradingProperties tradingProperties;

    protected BigDecimal getProfit(BigDecimal buyPrice, double lot, BigDecimal currentPrice) {
        BigDecimal buyLotPrice = MathUtils.multiply(buyPrice, lot);
        BigDecimal buyPricePlusCommission = MathUtils.addFraction(buyLotPrice, tradingProperties.getCommission());

        BigDecimal currentLotPrice = MathUtils.multiply(currentPrice, lot);
        BigDecimal sellPriceMinusCommission = MathUtils.subtractFraction(
                currentLotPrice, tradingProperties.getCommission());

        BigDecimal profit = MathUtils.getFractionDifference(sellPriceMinusCommission, buyPricePlusCommission);

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

}
