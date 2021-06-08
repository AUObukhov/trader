package ru.obukhov.trader.bot.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.bot.model.StrategyConfig;
import ru.obukhov.trader.bot.model.StrategyType;
import ru.obukhov.trader.bot.strategy.TradingStrategy;
import ru.obukhov.trader.bot.strategy.impl.ConservativeStrategy;
import ru.obukhov.trader.bot.strategy.impl.GoldenCrossStrategy;
import ru.obukhov.trader.bot.strategy.impl.GreedyGoldenCrossStrategy;

import java.util.Map;

class TradingStrategyFactoryUnitTest {

    private final TradingStrategyFactory factory = new TradingStrategyFactory(null);

    @Test
    void createStrategy_createsConservativeStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE);

        final TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(ConservativeStrategy.class, strategy.getClass());
    }

    // region GoldenCross strategy creation tests

    @Test
    void createStrategy_createsGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
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
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "bigWindow", 100,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndBigWindowIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGoldenCrossAndIndexCoefficientIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    // endregion

    // region GreedyGoldenCross strategy creation tests

    @Test
    void createStrategy_createsGreedyGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GREEDY_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100,
                "indexCoefficient", 0.5
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(GreedyGoldenCrossStrategy.class, strategy.getClass());
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGreedyGoldenCrossAndSmallWindowIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GREEDY_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "bigWindow", 100,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGreedyGoldenCrossAndBigWindowIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GREEDY_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "indexCoefficient", 0.5
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    @Test
    void createStrategy_throwsRuntimeException_whenGreedyGoldenCrossAndIndexCoefficientIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.GREEDY_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "smallWindow", 200,
                "bigWindow", 100
        ));

        Assertions.assertThrows(RuntimeException.class, () -> factory.createStrategy(strategyConfig));
    }

    // endregion

}