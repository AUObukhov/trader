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

    // region ConservativeStrategy strategy creation tests

    @Test
    void createStrategy_createsConservativeStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE);
        strategyConfig.setParams(Map.of("minimumProfit", 0.1));

        final TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(ConservativeStrategy.class, strategy.getClass());
    }

    @Test
    void createStrategy_throwIllegalArgumentException_whenMinimumProfitIsNull() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.CONSERVATIVE);
        strategyConfig.setParams(Map.of());

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyConfig),
                IllegalArgumentException.class,
                "minimumProfit is mandatory"
        );
    }

    // endregion

    // region SimpleGoldenCrossStrategy creation tests

    @Test
    void createStrategy_createsSimpleGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.SIMPLE_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "minimumProfit", 0.1,
                "indexCoefficient", 0.5,
                "greedy", false,
                "smallWindow", 100,
                "bigWindow", 200
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(SimpleGoldenCrossStrategy.class, strategy.getClass());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "minimumProfit is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", -0.1f,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 1.1f,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 7,
                                "bigWindow", 6
                        ),
                        "smallWindow must lower than bigWindow"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "bigWindow", 6
                        ),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", -1,
                                "bigWindow", 6
                        ),
                        "smallWindow min value is 1"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 0,
                                "bigWindow", 6
                        ),
                        "smallWindow min value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 3
                        ),
                        "bigWindow is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenGoldenCross_andParamsAreNotValid(
            Map<String, Object> params,
            String expectedMessage
    ) {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.SIMPLE_GOLDEN_CROSS);
        strategyConfig.setParams(params);

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyConfig),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenSimpleGoldenCross_andParamsAreEmpty() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.SIMPLE_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of());

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyConfig)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

    // region LinearGoldenCrossStrategy creation tests

    @Test
    void createStrategy_createsLinearGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.LINEAR_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "minimumProfit", 0.1,
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
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.LINEAR_GOLDEN_CROSS);
        strategyConfig.setParams(params);

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyConfig),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenStrategyTypeIsLinearGoldenCross_andParamsAreEmpty() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.LINEAR_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of());

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyConfig)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

    // region ExponentialGoldenCrossStrategy creation tests

    @Test
    void createStrategy_createsExponentialGoldenCrossStrategy() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.EXPONENTIAL_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of(
                "minimumProfit", 0.1,
                "fastWeightDecrease", 0.6,
                "slowWeightDecrease", 0.3,
                "indexCoefficient", 0.5,
                "greedy", false
        ));

        TradingStrategy strategy = factory.createStrategy(strategyConfig);

        Assertions.assertEquals(ExponentialGoldenCrossStrategy.class, strategy.getClass());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "fastWeightDecrease", 0.6,
                                "slowWeightDecrease", 0.3
                        ),
                        "minimumProfit is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "greedy", false,
                                "fastWeightDecrease", 0.6,
                                "slowWeightDecrease", 0.3
                        ),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", -0.1f,
                                "greedy", false,
                                "fastWeightDecrease", 0.6,
                                "slowWeightDecrease", 0.3
                        ),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 1.1f,
                                "greedy", false,
                                "fastWeightDecrease", 0.6,
                                "slowWeightDecrease", 0.3
                        ),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "fastWeightDecrease", 0.6,
                                "slowWeightDecrease", 0.3
                        ),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "slowWeightDecrease", 0.3
                        ),
                        "fastWeightDecrease is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "fastWeightDecrease", 1.1,
                                "slowWeightDecrease", 0.3
                        ),
                        "fastWeightDecrease max value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "fastWeightDecrease", 0.3,
                                "slowWeightDecrease", 0.6
                        ),
                        "slowWeightDecrease must lower than fastWeightDecrease"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "fastWeightDecrease", 0.6
                        ),
                        "slowWeightDecrease is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "fastWeightDecrease", 0.6,
                                "slowWeightDecrease", -0.1
                        ),
                        "slowWeightDecrease min value is 0"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreNotValid(
            Map<String, Object> params,
            String expectedMessage
    ) {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.EXPONENTIAL_GOLDEN_CROSS);
        strategyConfig.setParams(params);

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyConfig),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreEmpty() {
        final StrategyConfig strategyConfig = new StrategyConfig(StrategyType.EXPONENTIAL_GOLDEN_CROSS);
        strategyConfig.setParams(Map.of());

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyConfig)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("fastWeightDecrease is mandatory"));
        Assertions.assertTrue(message.contains("slowWeightDecrease is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

}