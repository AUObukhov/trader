package ru.obukhov.trader.bot.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.MathUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.model.Candle;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Decider which decides to buy and sell paper in some time after trend reversal.<br/>
 * Delay before decision to buy/sell and size of analysed trend are provided via constructor.
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
        Decision decision;
        if (data.getCurrentCandles().size() < lastPricesCount) {
            decision = Decision.WAIT_DECISION;
            log.warn("Need at least {} candles. Got {}. Decision is {}",
                    lastPricesCount, data.getCurrentCandles().size(), decision.toPrettyString());
        } else if (existsOperationInProgress(data)) {
            decision = Decision.WAIT_DECISION;
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
        } else if (data.getPosition() == null) {
            decision = getBuyOrWaitDecision(data);
        } else {
            decision = getSellOrWaitDecision(data);
            if (decision.getAction() == DecisionAction.WAIT) {
                decision = getBuyOrWaitDecision(data);
            }
        }

        return decision;
    }

    private Decision getBuyOrWaitDecision(DecisionData data) {
        Decision decision;
        List<Candle> currentCandles = CollectionsUtils.getTail(data.getCurrentCandles(), lastPricesCount);
        int availableLots = getAvailableLots(data);
        if (availableLots > 0) {
            final BigDecimal minPrice = getMinPrice(currentCandles);
            final BigDecimal expectedMinPrice = getExpectedExtremumCandle(currentCandles).getLowestPrice();
            if (MathUtils.numbersEqual(expectedMinPrice, minPrice)) {
                decision = new Decision(DecisionAction.BUY, availableLots);
                log.debug("expectedMinPrice {} is equal to minPrice {}. Decision is {}",
                        expectedMinPrice, minPrice, decision.toPrettyString());
            } else {
                decision = Decision.WAIT_DECISION;
                log.debug("expectedMinPrice {} is not equal to minPrice {}. Decision is {}",
                        expectedMinPrice, minPrice, decision.toPrettyString());
            }
        } else {
            decision = Decision.WAIT_DECISION;
            log.debug("No position and current balance {} is not enough to buy any lots. Decision is {}",
                    data.getBalance(), decision.toPrettyString());
        }
        return decision;
    }

    private Decision getSellOrWaitDecision(DecisionData data) {
        double profit = getProfit(data);
        Decision decision;
        if (profit < MINIMUM_PROFIT) {
            decision = Decision.WAIT_DECISION;
            log.debug("Profit {} is lower than minimum profit {}. Decision is {}",
                    profit, MINIMUM_PROFIT, decision.toPrettyString());
        } else {
            List<Candle> currentCandles = CollectionsUtils.getTail(data.getCurrentCandles(), lastPricesCount);
            final BigDecimal maxPrice = getMaxPrice(currentCandles);
            final BigDecimal expectedMaxPrice = getExpectedExtremumCandle(currentCandles).getHighestPrice();

            if (MathUtils.numbersEqual(expectedMaxPrice, maxPrice)) {
                decision = new Decision(DecisionAction.SELL, data.getPositionLotsCount());
                log.debug("expectedMaxPrice {} is equal to maxPrice {}. Decision is {}",
                        expectedMaxPrice, maxPrice, decision.toPrettyString());

            } else {
                decision = Decision.WAIT_DECISION;
                log.debug("expectedMaxPrice {} is not equal to maxPrice {}. Decision is {}",
                        expectedMaxPrice, maxPrice, decision.toPrettyString());
            }
        }

        return decision;
    }

    private BigDecimal getMinPrice(List<Candle> currentCandles) {
        return currentCandles.stream()
                .map(Candle::getLowestPrice)
                .min(Comparator.naturalOrder())
                .orElseThrow();
    }

    private BigDecimal getMaxPrice(List<Candle> currentCandles) {
        return currentCandles.stream()
                .map(Candle::getHighestPrice)
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }

    private Candle getExpectedExtremumCandle(List<Candle> candles) {
        return candles.get(extremumPriceIndex);
    }

}