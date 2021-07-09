package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

import java.util.Map;
import java.util.stream.Stream;

class TradingStrategyFactoryUnitTest {

    private final TradingStrategyFactory factory = new TradingStrategyFactory(null);

    // region ConservativeStrategy strategy creation tests

    @Test
    void createStrategy_createsConservativeStrategy() {
        final StrategyType strategyType = StrategyType.CONSERVATIVE;
        final Map<String, Object> params = Map.of("minimumProfit", 0.1);

        final TradingStrategy strategy = factory.createStrategy(strategyType, params);

        Assertions.assertEquals(ConservativeStrategy.class, strategy.getClass());
    }

    @Test
    void createStrategy_throwIllegalArgumentException_whenConservative_andMinimumProfitIsNull() {
        final StrategyType strategyType = StrategyType.CONSERVATIVE;
        final Map<String, Object> params = Map.of();

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyType, params),
                IllegalArgumentException.class,
                "minimumProfit is mandatory"
        );
    }

    // endregion

    // region SimpleGoldenCrossStrategy creation tests

    @Test
    void createStrategy_createsSimpleGoldenCrossStrategy() {
        final StrategyType strategyType = StrategyType.SIMPLE_GOLDEN_CROSS;
        final Map<String, Object> params = Map.of(
                "minimumProfit", 0.1,
                "order", 1,
                "indexCoefficient", 0.5,
                "greedy", false,
                "smallWindow", 100,
                "bigWindow", 200
        );

        TradingStrategy strategy = factory.createStrategy(strategyType, params);

        Assertions.assertEquals(SimpleGoldenCrossStrategy.class, strategy.getClass());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "order", 1,
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
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "order is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", -1,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "order min value is 1"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 0,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "order min value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
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
                                "order", 1,
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
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
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
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "bigWindow", 6
                        ),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", -1,
                                "bigWindow", 6
                        ),
                        "smallWindow must be positive"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 0,
                                "bigWindow", 6
                        ),
                        "smallWindow must be positive"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
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
            final Map<String, Object> params,
            final String expectedMessage
    ) {
        final StrategyType strategyType = StrategyType.SIMPLE_GOLDEN_CROSS;

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyType, params),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenSimpleGoldenCross_andParamsAreEmpty() {
        final StrategyType strategyType = StrategyType.SIMPLE_GOLDEN_CROSS;
        final Map<String, Object> params = Map.of();

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyType, params)
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
        final StrategyType strategyType = StrategyType.LINEAR_GOLDEN_CROSS;
        final Map<String, Object> params = Map.of(
                "minimumProfit", 0.1,
                "order", 1,
                "smallWindow", 100,
                "bigWindow", 200,
                "indexCoefficient", 0.5,
                "greedy", false
        );

        TradingStrategy strategy = factory.createStrategy(strategyType, params);

        Assertions.assertEquals(LinearGoldenCrossStrategy.class, strategy.getClass());
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenSimpleOrLinearGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenLinearGoldenCross_andParamsAreNotValid(
            final Map<String, Object> params,
            final String expectedMessage
    ) {
        final StrategyType strategyType = StrategyType.LINEAR_GOLDEN_CROSS;

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyType, params),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenStrategyTypeIsLinearGoldenCross_andParamsAreEmpty() {
        final StrategyType strategyType = StrategyType.LINEAR_GOLDEN_CROSS;
        final Map<String, Object> params = Map.of();

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyType, params)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("order is mandatory"));
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

    // region ExponentialGoldenCrossStrategy creation tests

    @Test
    void createStrategy_createsExponentialGoldenCrossStrategy() {
        final StrategyType strategyType = StrategyType.EXPONENTIAL_GOLDEN_CROSS;
        final Map<String, Object> params = Map.of(
                "minimumProfit", 0.1,
                "order", 1,
                "indexCoefficient", 0.5,
                "greedy", false,
                "smallWindow", 2,
                "bigWindow", 5
        );

        TradingStrategy strategy = factory.createStrategy(strategyType, params);

        Assertions.assertEquals(ExponentialGoldenCrossStrategy.class, strategy.getClass());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "minimumProfit is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "order is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", -1,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "order min value is 1"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 0,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "order min value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "indexCoefficient is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", -0.1f,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "indexCoefficient min value is 0"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 1.1f,
                                "greedy", false,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "indexCoefficient max value is 1"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "smallWindow", 2,
                                "bigWindow", 5
                        ),
                        "greedy is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "bigWindow", 5
                        ),
                        "smallWindow is mandatory"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", -1,
                                "bigWindow", 5
                        ),
                        "smallWindow must be positive"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 0,
                                "bigWindow", 5
                        ),
                        "smallWindow must be positive"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 5,
                                "bigWindow", 2
                        ),
                        "smallWindow must lower than bigWindow"
                ),
                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 2
                        ),
                        "bigWindow is mandatory"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreNotValid(
            final Map<String, Object> params,
            final String expectedMessage
    ) {
        final StrategyType strategyType = StrategyType.EXPONENTIAL_GOLDEN_CROSS;

        AssertUtils.assertThrowsWithMessage(
                () -> factory.createStrategy(strategyType, params),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenExponentialGoldenCross_andParamsAreEmpty() {
        final StrategyType strategyType = StrategyType.EXPONENTIAL_GOLDEN_CROSS;
        final Map<String, Object> params = Map.of();

        final IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> factory.createStrategy(strategyType, params)
        );

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
    }

    // endregion

}