package ru.obukhov.investor.bot.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.obukhov.investor.Decision;
import ru.obukhov.investor.bot.interfaces.Decider;
import ru.obukhov.investor.bot.model.DecisionData;
import ru.obukhov.investor.util.MathUtils;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.portfolio.Portfolio;

import java.math.BigDecimal;

@Slf4j
@Service
public class DeciderImpl implements Decider {

    private static final long MINIMUM_YIELD = 1L;
    private static final double COMMISSION = 0.05;

    @Override
    public Decision decide(DecisionData data) {
        if (existsOperationInProgress(data)) {
            log.debug("Exists operation in progress. Decision is Wait");
            return Decision.WAIT;
        }

        if (MathUtils.isGreater(data.getCurrentPrice(), data.getBalance())) {
            log.debug("Current price + " + data.getCurrentPrice() + " is greater than balance + " + data.getBalance()
                    + ". Decision is Wait");
            return Decision.WAIT;
        }

        final Portfolio.PortfolioPosition position = data.getPosition();
        if (position == null) {
            log.debug("No position. Decision is Buy");
            return Decision.BUY;
        }

        Assert.notNull(position.expectedYield, "expectedYield must be not null");

        BigDecimal yield = MathUtils.subtractCommission(position.expectedYield.value, COMMISSION);

        if (MathUtils.isGreater(yield, MINIMUM_YIELD)) {
            log.debug("Expected yield " + yield + " is greater than minimum " + MINIMUM_YIELD + ". Decision is Sell");
            return Decision.SELL;
        } else {
            log.debug("Expected yield " + yield + " is not greater than minimum " + MINIMUM_YIELD
                    + ". Decision is Wait");
            return Decision.WAIT;
        }
    }

    private boolean existsOperationInProgress(DecisionData data) {
        return data.getLastOperations().stream()
                .anyMatch(operation -> operation.status == OperationStatus.Progress);
    }

}