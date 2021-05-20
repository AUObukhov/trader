package ru.obukhov.trader.bot.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.bot.model.StrategyType;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;

import java.util.Map;

class TradingStrategyFactoryUnitTest {

    private final TradingStrategyFactory factory = new TradingStrategyFactory(null);

    @Test
    void createStrategy_createsConservativeStrategy() {
        StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE);

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(ConservativeStrategy.class, strategy.getClass());
    }

    // region GoldenCross strategy creation tests

    @Test
    void createStrategy_createsGoldenCrossStrategy() {
        StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100,
                "indexCoefficient", 0.5
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(GoldenCrossStrategy.class, strategy.getClass());
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndSmallWindowIsNull() {
        StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "bigWindow", 100,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndBigWindowIsNull() {
        StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndIndexCoefficientIsNull() {
        StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    // endregion

}