package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.bot.model.Decision;
import ru.obukhov.investor.bot.model.DecisionAction;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.util.CollectionsUtils;
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
@Getter
public class TrendReversalDecider extends AbstractDecider {

    private final Integer lastPricesCount;
    private final Integer extremumPriceIndex;

    public TrendReversalDecider(TradingProperties tradingProperties,
                                Integer lastPricesCount,
                                Integer extremumPriceIndex) {
        super(tradingProperties);
        this.lastPricesCount = lastPricesCount;
        this.extremumPriceIndex = extremumPriceIndex;
    }

    @Override
    public Decision decide(DecisionData data) {
        if (data.getCurrentCandles().size() < lastPricesCount) {
            log.debug("Need at least {} candles. Got {}. Decision is Wait", lastPricesCount, data.getCurrentCandles().size());
            return Decision.WAIT_DECISION;
        }
        if (existsOperationInProgress(data)) {
            log.debug("Exists operation in progress. Decision is Wait");
            return Decision.WAIT_DECISION;
        }

        final Portfolio.PortfolioPosition position = data.getPosition();
        List<Candle> currentCandles = CollectionsUtils.getTail(data.getCurrentCandles(), lastPricesCount);
        final BigDecimal currentPrice = Iterables.getLast(currentCandles).getClosePrice();
        final BigDecimal currentPriceWithCommission =
                MathUtils.addFraction(currentPrice, tradingProperties.getCommission());
        if (position == null) {
            if (MathUtils.isGreater(currentPriceWithCommission, data.getBalance())) {
                log.debug("No position, but current price with commission = {} is greater than balance {}." +
                                " Decision is Wait",
                        currentPriceWithCommission, data.getBalance());
                return Decision.WAIT_DECISION;
            } else {
                final BigDecimal max = currentCandles.stream()
                        .map(Candle::getHighestPrice)
                        .max(Comparator.naturalOrder())
                        .orElseThrow();
                final BigDecimal expectedMax = getExpectedExtremumCandle(currentCandles).getHighestPrice();
                if (MathUtils.numbersEqual(expectedMax, max)) {
                    Decision decision = new Decision(DecisionAction.BUY, 1);
                    log.debug("Expected max {} is equal to max {}. Decision is {}", expectedMax, max, decision);
                    return decision;
                } else {
                    log.debug("Expected max {} is not equal to max {}. Decision is Wait", expectedMax, max);
                    return Decision.WAIT_DECISION;
                }
            }
        }

        BigDecimal profit = getProfit(position.averagePositionPrice.value, data.getInstrument().lot, currentPrice);
        if (MathUtils.isLower(profit, MINIMUM_PROFIT)) {
            log.debug("Profit {} is lower than minimum profit {}. Decision is Wait", profit, MINIMUM_PROFIT);
            return Decision.WAIT_DECISION;
        }

        final BigDecimal min = currentCandles.stream()
                .map(Candle::getLowestPrice)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        final BigDecimal expectedMin = getExpectedExtremumCandle(currentCandles).getLowestPrice();
        if (MathUtils.numbersEqual(expectedMin, min)) {
            Decision decision = new Decision(DecisionAction.SELL, 1);
            log.debug("Expected min {} is equal to min {}. Decision is {}", expectedMin, min, decision);
            return decision;
        } else {
            log.debug("Expected min {} is not equal to min {}. Decision is Wait", expectedMin, min);
            return Decision.WAIT_DECISION;
        }
    }

    private Candle getExpectedExtremumCandle(List<Candle> candles) {
        return candles.get(extremumPriceIndex);
    }

}