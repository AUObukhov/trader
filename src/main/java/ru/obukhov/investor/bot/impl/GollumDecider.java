package ru.obukhov.investor.bot.impl;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.config.TradingProperties;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;

/**
 * Decider which decides to buy paper as soon as possible and never sell
 */
@Slf4j
public class GollumDecider extends AbstractDecider {

    public GollumDecider(TradingProperties tradingProperties) {
        super(tradingProperties);
    }

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

        return Decision.WAIT;
    }

}