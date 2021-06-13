package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.trading.model.StrategyConfig;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

import java.util.Map;

class TradingStrategyFactoryUnitTest {

    private final TradingStrategyFactory factory = new TradingStrategyFactory(null);

    @Test
    void createStrategy_createsConservativeStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE, 0.1f);

        final TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(ConservativeStrategy.class, strategy.getClass());
    }

    // region GoldenCross strategy creation tests

    @Test
    void createStrategy_createsGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100,
                "indexCoefficient", 0.5,
                "greedy", false
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(GoldenCrossStrategy.class, strategy.getClass());
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndSmallWindowIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "bigWindow", 100,
                "indexCoefficient", 0.5,
                "greedy", false
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndBigWindowIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "indexCoefficient", 0.5,
                "greedy", false
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndIndexCoefficientIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100,
                "greedy", false
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndGreedyIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    // endregion

}