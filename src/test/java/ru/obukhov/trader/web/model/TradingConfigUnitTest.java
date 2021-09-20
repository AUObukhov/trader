package ru.obukhov.trader.web.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;

import java.util.Map;
import java.util.stream.Stream;

class TradingConfigUnitTest {

    // region equals tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forEquals() {
        return Stream.of(
                Arguments.of(
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        true
                ),
                Arguments.of(
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new TradingConfig().setCandleResolution(CandleResolution._2MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        false
                ),
                Arguments.of(
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CROSS)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        false
                ),
                Arguments.of(
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1)),
                        false
                ),
                Arguments.of(
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param", 2)),
                        false
                ),
                Arguments.of(
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 2)),
                        new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                                .setStrategyType(StrategyType.CONSERVATIVE)
                                .setStrategyParams(Map.of("param1", 1, "param2", 22)),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forEquals")
    void testEquals(TradingConfig config1, TradingConfig config2, boolean expectedResult) {
        boolean result = config1.equals(config2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    @Test
    void testHashCode() {
        TradingConfig config1 = new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("param1", 1, "param2", 2));
        TradingConfig config2 = new TradingConfig().setCandleResolution(CandleResolution._1MIN)
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of("param1", 1, "param2", 2));

        Assertions.assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    void testToString() {
        final TradingConfig config = new TradingConfig()
                .setBrokerAccountId("2000124699")
                .setTicker("ticker")
                .setCandleResolution(CandleResolution._1MIN)
                .setCommission(0.003)
                .setStrategyType(StrategyType.CROSS)
                .setStrategyParams(Map.of(
                        "minimumProfit", 0.01,
                        "movingAverageType", MovingAverageType.LINEAR_WEIGHTED,
                        "smallWindow", 100,
                        "bigWindow", 200,
                        "indexCoefficient", 0.3,
                        "greedy", false
                ));

        final String string = config.toString();

        final String expectedStart = "[brokerAccountId=2000124699, ticker=ticker, candleResolution=1min, commission=0.003, strategyType=cross, " +
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
