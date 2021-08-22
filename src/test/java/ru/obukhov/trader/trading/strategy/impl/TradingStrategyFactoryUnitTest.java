package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;

import java.util.Map;
import java.util.stream.Stream;

class TradingStrategyFactoryUnitTest {

    private final TradingStrategyFactory factory = new TradingStrategyFactory(null);

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_givesStrategyProperName() {
        return Stream.of(
                Arguments.of(
                        StrategyType.CONSERVATIVE,
                        Map.of("minimumProfit", 0.1),
                        "conservative [minimumProfit=0.1]"
                ),
                Arguments.of(
                        StrategyType.GOLDEN_CROSS,
                        Map.of(
                                "minimumProfit", 0.1,
                                "movingAverageType", MovingAverageType.SIMPLE,
                                "order", 1,
                                "indexCoefficient", 0.4,
                                "greedy", false,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "goldenCross [minimumProfit=0.1, movingAverageType=SMA, order=1, indexCoefficient=0.4, greedy=false, smallWindow=100, bigWindow=200]"
                ),
                Arguments.of(
                        StrategyType.GOLDEN_CROSS,
                        Map.of(
                                "minimumProfit", 0.1,
                                "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                                "order", 2,
                                "indexCoefficient", 0.5,
                                "greedy", true,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "goldenCross [minimumProfit=0.1, movingAverageType=LWMA, order=2, indexCoefficient=0.5, greedy=true, smallWindow=100, bigWindow=200]"
                ),
                Arguments.of(
                        StrategyType.GOLDEN_CROSS,
                        Map.of(
                                "minimumProfit", 0.2,
                                "movingAverageType", MovingAverageType.EXPONENTIAL_WEIGHTED,
                                "order", 1,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 10,
                                "bigWindow", 20
                        ),
                        "goldenCross [minimumProfit=0.2, movingAverageType=EWMA, order=1, indexCoefficient=0.5, greedy=false, smallWindow=10, bigWindow=20]"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_givesStrategyProperName")
    void createStrategy_givesStrategyProperName(final StrategyType strategyType, final Map<String, Object> params, final String expectedName) {
        TradingStrategy strategy = factory.createStrategy(strategyType, params);

        Assertions.assertEquals(expectedName, strategy.getName());
    }

    // region ConservativeStrategy strategy creation tests

    @Test
    void createStrategy_throwIllegalArgumentException_whenConservative_andMinimumProfitIsNull() {
        final StrategyType strategyType = StrategyType.CONSERVATIVE;
        final Map<String, Object> params = Map.of();

        final Executable executable = () -> factory.createStrategy(strategyType, params);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "minimumProfit is mandatory");
    }

    // endregion

    // region GoldenCrossStrategy creation tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenGoldenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "movingAverageType is mandatory"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
                                "movingAverageType", MovingAverageType.SIMPLE,
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
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenGoldenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenGoldenCross_andParamsAreNotValid(
            final Map<String, Object> params,
            final String expectedMessage
    ) {
        final StrategyType strategyType = StrategyType.GOLDEN_CROSS;

        final Executable executable = () -> factory.createStrategy(strategyType, params);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenGoldenCross_andParamsAreEmpty() {
        final StrategyType strategyType = StrategyType.GOLDEN_CROSS;
        final Map<String, Object> params = Map.of();

        final Executable executable = () -> factory.createStrategy(strategyType, params);
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("movingAverageType is mandatory"));
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

}