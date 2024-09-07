package ru.obukhov.trader.trading.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.trading.model.Decision;
import ru.obukhov.trader.trading.model.DecisionAction;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.obukhov.trader.trading.model.DecisionsData;
import ru.obukhov.trader.web.model.BotConfig;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategy which decides to buy security always when possible and never to sell
 */
@Slf4j
public class ConservativeStrategy extends AbstractTradingStrategy {

    public ConservativeStrategy(final String name) {
        super(name, null);
    }

    @Override
    public Map<String, Decision> decide(final DecisionsData data, final BotConfig botConfig, final Interval interval) {
        Assert.isTrue(data.getDecisionDatas().size() == 1, "Conservative strategy supports 1 instrument only");

        if (existsOperationStateIsUnspecified(data)) {
            Decision decision = new Decision(DecisionAction.WAIT, null);
            log.debug("Exists operation in progress. Decision is {}", decision.toPrettyString());
            return data.getDecisionDatas().stream()
                    .collect(Collectors.toMap(decisionData -> decisionData.getShare().figi(), decisionData -> decision));
        } else {
            final DecisionData decisionData = data.getDecisionDatas().getFirst();

            if (decisionData.getAvailableLots() == 0) {
                final Decision decision = new Decision(DecisionAction.WAIT, null);
                return Map.of(decisionData.getShare().figi(), decision);
            }

            final Decision decision = getBuyOrWaitDecision(decisionData, decisionData.getAvailableLots());
            return Map.of(decisionData.getShare().figi(), decision);
        }
    }

}