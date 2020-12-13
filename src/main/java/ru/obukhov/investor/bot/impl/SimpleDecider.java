package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;

@Slf4j
@RequiredArgsConstructor
public class SimpleDecider implements Decider {

    protected static final double MINIMUM_PROFIT = 0.01;

    private final TradingProperties tradingProperties;

    @Override
    public Decision decide(DecisionData data) {
        if (existsOperationInProgress(data)) {
            log.debug("Exists operation in progress. Decision is Wait");
            return Decision.WAIT;
        }

        final Portfolio.PortfolioPosition position = data.getPosition();
        final BigDecimal currentPrice = Iterables.getLast(data.getCurrentPrices());
        if (position == null) {
            if (MathUtils.isGreater(currentPrice, data.getBalance())) {
                log.debug("Current price = {} is greater than balance {}. Decision is Wait",
                        currentPrice, data.getBalance());
                return Decision.WAIT;
            } else {
                log.debug("No position. Decision is Buy");
                return Decision.BUY;
            }
        }

        BigDecimal profit = getProfit(position.averagePositionPrice.value, data.getInstrument().lot, currentPrice);

        return MathUtils.isGreater(profit, MINIMUM_PROFIT) ? Decision.SELL : Decision.WAIT;
    }

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

    protected boolean existsOperationInProgress(DecisionData data) {
        return data.getLastOperations().stream()
                .anyMatch(operation -> operation.status == OperationStatus.Progress);
    }

}