package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.util.Map;
import java.util.stream.Stream;

class BotConfigUnitTest {

    // region equals tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEquals() {
        return Stream.of(
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        true
                ),
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_5_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        false
                ),
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.1)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.2)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        false
                ),
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CROSS)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        false
                ),
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1))
                                .build(),
                        false
                ),
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param", 2))
                                .build(),
                        false
                ),
                Arguments.of(
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 2))
                                .build(),
                        BotConfig.builder()
                                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                                .commission(0.0)
                                .strategyType(StrategyType.CONSERVATIVE)
                                .strategyParams(Map.of("param1", 1, "param2", 22))
                                .build(),
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
        BotConfig config1 = BotConfig.builder()
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.0)
                .strategyType(StrategyType.CONSERVATIVE)
                .strategyParams(Map.of("param1", 1, "param2", 2))
                .build();
        BotConfig config2 = BotConfig.builder()
                .candleInterval(CandleInterval.CANDLE_INTERVAL_1_MIN)
                .commission(0.0)
                .strategyType(StrategyType.CONSERVATIVE)
                .strategyParams(Map.of("param1", 1, "param2", 2))
                .build();

        Assertions.assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        final BotConfig config = new BotConfig(
                "2000124699",
                "ticker",
                CandleInterval.CANDLE_INTERVAL_1_MIN,
                0.003,
                StrategyType.CROSS,
                Map.of(
                        "minimumProfit", 0.01,
                        "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                )
        );

        final String string = config.toString();

        final String expectedStart = "[brokerAccountId=2000124699, ticker=ticker, candleInterval=CANDLE_INTERVAL_1_MIN, commission=0.003, strategyType=cross, " +
                "strategyParams={";
        Assertions.assertTrue(string.startsWith(expectedStart));
        Assertions.assertTrue(string.contains("minimumProfit=0.01"));
        Assertions.assertTrue(string.contains("movingAverageType=LWMA"));
        Assertions.assertTrue(string.contains("smallWindow=100"));
        Assertions.assertTrue(string.contains("bigWindow=200"));
        Assertions.assertTrue(string.contains("greedy=false"));
        Assertions.assertTrue(string.contains("indexCoefficient=0.3"));
    }

}
