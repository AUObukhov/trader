package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.config.TrendReversalDeciderProperties;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Decider which decider to buy and sell paper when some time after trend reversal.<br/>
 * Delay before decision to buy/sell and trend length are provided via constructor.
 */
@Slf4j
public class TrendReversalDecider extends AbstractDecider {

    public final TrendReversalDeciderProperties deciderProperties;

    public TrendReversalDecider(TradingProperties tradingProperties, TrendReversalDeciderProperties deciderProperties) {
        super(tradingProperties);
        this.deciderProperties = deciderProperties;
    }

    @Override
    public Decision decide(DecisionData data) {
        if (data.getCurrentPrices().size() < deciderProperties.getLastPricesCount()) {
            log.debug("Need at least " + deciderProperties.getLastPricesCount() + " prices." +
                    " Got " + data.getCurrentPrices().size() + ". Decision is Wait");
            return Decision.WAIT;
        }
        if (existsOperationInProgress(data)) {
            log.debug("Exists operation in progress. Decision is Wait");
            return Decision.WAIT;
        }

        final Portfolio.PortfolioPosition position = data.getPosition();
        final BigDecimal currentPrice = Iterables.getLast(data.getCurrentPrices());
        final BigDecimal currentPriceWithCommission =
                MathUtils.addFraction(currentPrice, tradingProperties.getCommission());
        if (position == null) {
            if (MathUtils.isGreater(currentPriceWithCommission, data.getBalance())) {
                log.debug("No position, but current price with commission = {} is greater than balance {}." +
                                " Decision is Wait",
                        currentPriceWithCommission, data.getBalance());
                return Decision.WAIT;
            } else {
                final BigDecimal max = data.getCurrentPrices().stream().max(Comparator.naturalOrder()).orElseThrow();
                final BigDecimal expectedMax = getExpectedExtremum(data.getCurrentPrices());
                if (MathUtils.numbersEqual(expectedMax, max)) {
                    log.debug("Expected max " + expectedMax + " is equal to max " + max + ". Decision is Buy");
                    return Decision.BUY;
                } else {
                    log.debug("Expected max " + expectedMax + " is not equal to max " + max + ". Decision is Wait");
                    return Decision.WAIT;
                }
            }
        }

        BigDecimal profit = getProfit(position.averagePositionPrice.value, data.getInstrument().lot, currentPrice);
        if (MathUtils.isLower(profit, MINIMUM_PROFIT)) {
            log.debug("Profit " + profit + " is lower than minimum profit " + profit + ". Decision is Wait");
            return Decision.WAIT;
        }

        final BigDecimal min = data.getCurrentPrices().stream().min(Comparator.naturalOrder()).orElseThrow();
        final BigDecimal expectedMin = getExpectedExtremum(data.getCurrentPrices());
        if (MathUtils.numbersEqual(expectedMin, min)) {
            log.debug("Expected min " + expectedMin + " is equal to min " + min + ". Decision is Sell");
            return Decision.SELL;
        } else {
            log.debug("Expected min " + expectedMin + " is not equal to min " + min + ". Decision is Wait");
            return Decision.WAIT;
        }
    }

    private BigDecimal getExpectedExtremum(List<BigDecimal> prices) {
        int index = prices.size() - deciderProperties.getLastPricesCount() + deciderProperties.getExtremumPriceIndex();
        return prices.get(index);
    }

}