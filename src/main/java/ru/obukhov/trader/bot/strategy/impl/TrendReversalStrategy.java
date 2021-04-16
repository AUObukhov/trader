package ru.obukhov.trader.bot.strategy.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.trader.bot.model.Decision;
import ru.obukhov.trader.bot.model.DecisionAction;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.bot.strategy.AbstractStrategy;
import ru.obukhov.trader.common.util.CollectionsUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.TradingProperties;
import ru.obukhov.trader.market.model.Candle;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy which decides to buy and sell paper in some time after trend reversal.<br/>
 * Delay before decision to buy/sell and size of analysed trend are provided via constructor.
 */
@Slf4j
@Getter
public class TrendReversalStrategy extends AbstractStrategy {

    private final Integer lastPricesCount;
    private final Integer extremumPriceIndex;

    public TrendReversalStrategy(TradingProperties tradingProperties,
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

    @Override
    protected Decision getBuyOrWaitDecision(DecisionData data) {
        Decision decision;
        List<Candle> currentCandles = CollectionsUtils.getTail(data.getCurrentCandles(), lastPricesCount);
        final BigDecimal minPrice = getMinPrice(currentCandles);
        final BigDecimal expectedMinPrice = getExpectedExtremumCandle(currentCandles).getLowestPrice();
        if (DecimalUtils.numbersEqual(expectedMinPrice, minPrice)) {
            decision = super.getBuyOrWaitDecision(data);
        } else {
            decision = Decision.WAIT_DECISION;
            log.debug("expectedMinPrice {} is not equal to minPrice {}. Decision is {}",
                    expectedMinPrice, minPrice, decision.toPrettyString());
        }

        return decision;
    }

    @Override
    protected Decision getSellOrWaitDecision(DecisionData data) {
        Decision decision;
        List<Candle> currentCandles = CollectionsUtils.getTail(data.getCurrentCandles(), lastPricesCount);
        final BigDecimal maxPrice = getMaxPrice(currentCandles);
        final BigDecimal expectedMaxPrice = getExpectedExtremumCandle(currentCandles).getHighestPrice();
        if (DecimalUtils.numbersEqual(expectedMaxPrice, maxPrice)) {
            decision = super.getSellOrWaitDecision(data);
        } else {
            decision = Decision.WAIT_DECISION;
            log.debug("expectedMaxPrice {} is not equal to maxPrice {}. Decision is {}",
                    expectedMaxPrice, maxPrice, decision.toPrettyString());
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