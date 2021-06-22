package ru.obukhov.trader.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.util.Map;
import java.util.stream.Stream;

class BotConfigUnitTest {

    // region equals tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEquals() {
        return Stream.of(
                Arguments.of(
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        true
                ),
                Arguments.of(
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new BotConfig().setCandleResolution(CandleResolution._2MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        false
                ),
                Arguments.of(
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.SIMPLE_GOLDEN_CROSS)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        false
                ),
                Arguments.of(
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1)),
                        false
                ),
                Arguments.of(
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param", 2)),
                        false
                ),
                Arguments.of(
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new BotConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 22)),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEquals")
    void testEquals(BotConfig config1, BotConfig config2, boolean expectedResult) {
        boolean result = config1.equals(config2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    @Test
    void testHashCode() {
        BotConfig config1 = new BotConfig().setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("param1", 1, "param2", 2));
        BotConfig config2 = new BotConfig().setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("param1", 1, "param2", 2));

        Assertions.assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        final BotConfig config = new BotConfig()
                .setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.LINEAR_GOLDEN_CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));

        final String string = config.toString();

        final String expectedStart = "[candleResolution=1min, strategyType=LINEAR_GOLDEN_CROSS, strategyParams={";
        Assertions.assertTrue(string.startsWith(expectedStart));
        Assertions.assertTrue(string.contains("minimumProfit=0.01"));
        Assertions.assertTrue(string.contains("smallWindow=100"));
        Assertions.assertTrue(string.contains("bigWindow=200"));
        Assertions.assertTrue(string.contains("greedy=false"));
        Assertions.assertTrue(string.contains("indexCoefficient=0.3"));
    }

}
