package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyConfig;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

import java.util.Map;
import java.util.stream.Stream;

class TradingStrategyFactoryUnitTest {

    private final TradingStrategyFactory factory = new TradingStrategyFactory(null);

    @Test
    void createStrategy_createsConservativeStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE, 0.1f);

        final TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(ConservativeStrategy.class, strategy.getClass());
    }

    // region SimpleGoldenCross strategy creation tests

    @Test
    void createStrategy_createsSimpleGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.SIMPLE_GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "smallWindow", 100,
                "bigWindow", 200,
                "indexCoefficient", 0.5,
                "greedy", false
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(SimpleGoldenCrossStrategy.class, strategy.getClass());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "smallWindow", 7,
                                "bigWindow", 6,
                                "indexCoefficient", 0.6f,
                                "greedy", false
                        ),
                        "smallWindow must not be greater than bigWindow"
                ),
                Arguments.of(
                        Map.of(
                                "bigWindow", 6,
                                "indexCoefficient", 0.6f,
                                "greedy", false
                        ),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", -1,
                                "bigWindow", 6,
                                "indexCoefficient", 0.6f,
                                "greedy", false
                        ),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", 0,
                                "bigWindow", 6,
                                "indexCoefficient", 0.6f,
                                "greedy", false
                        ),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", 3,
                                "indexCoefficient", 0.6f,
                                "greedy", false
                        ),
                        "bigWindow is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", 3,
                                "bigWindow", 6,
                                "greedy", false
                        ),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", 3,
                                "bigWindow", 6,
                                "indexCoefficient", -0.1f,
                                "greedy", false
                        ),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", 3,
                                "bigWindow", 6,
                                "indexCoefficient", 1.1f,
                                "greedy", false
                        ),
                        "indexCoefficient max value is 1"
                ),
                Arguments.of(
                        Map.of(
                                "smallWindow", 3,
                                "bigWindow", 6,
                                "indexCoefficient", 0.6f
                        ),
                        "greedy is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenGoldenCross_andParamsAreNotValid(
            Map<String, Object> params,
            String expectedMessage
    ) {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.SIMPLE_GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(params);

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyConfig),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenSimpleGoldenCross_andParamsAreEmpty() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.SIMPLE_GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of());

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyConfig)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

    // region LinearGoldenCross strategy creation tests

    @Test
    void createStrategy_createsLinearGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.LINEAR_GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of(
                "smallWindow", 100,
                "bigWindow", 200,
                "indexCoefficient", 0.5,
                "greedy", false
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(LinearGoldenCrossStrategy.class, strategy.getClass());
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenLinearGoldenCross_andParamsAreNotValid(
            Map<String, Object> params,
            String expectedMessage
    ) {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.LINEAR_GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(params);

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyConfig),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenStrategyTypeIsLinearGoldenCross_andParamsAreEmpty() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.LINEAR_GOLDEN_CROSS, 0.1f);
        strategyConfig.setParams(Map.of());

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyConfig)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

}