package ru.obukhov.trader.trading.strategy.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import ru.obukhov.trader.common.service.impl.MovingAverager;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.trading.model.StrategyType;
import ru.obukhov.trader.trading.strategy.interfaces.TradingStrategy;
import ru.obukhov.trader.web.model.TradingConfig;

import java.util.Map;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class TradingStrategyFactoryUnitTest {

    @Mock
    private ApplicationContext applicationContext;
    @InjectMocks
    private TradingStrategyFactory factory;

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_givesStrategyProperName() {
        return Stream.of(
                Arguments.of(
                        StrategyType.CONSERVATIVE,
                        Map.of("minimumProfit", 0.1),
                        "conservative [minimumProfit=0.1]"
                ),
                Arguments.of(
                        StrategyType.CROSS,
                        Map.of(
                                "minimumProfit", 0.1,
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                                "order", 1,
                                "indexCoefficient", 0.4,
                                "greedy", false,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "cross SMA [minimumProfit=0.1, order=1, indexCoefficient=0.4, greedy=false, smallWindow=100, bigWindow=200]"
                ),
                Arguments.of(
                        StrategyType.CROSS,
                        Map.of(
                                "minimumProfit", 0.1,
                                "movingAverageType", MovingAverageType.LINEAR_WEIGHTED.getValue(),
                                "order", 2,
                                "indexCoefficient", 0.5,
                                "greedy", true,
                                "smallWindow", 100,
                                "bigWindow", 200
                        ),
                        "cross LWMA [minimumProfit=0.1, order=2, indexCoefficient=0.5, greedy=true, smallWindow=100, bigWindow=200]"
                ),
                Arguments.of(
                        StrategyType.CROSS,
                        Map.of(
                                "minimumProfit", 0.2,
                                "movingAverageType", MovingAverageType.EXPONENTIAL_WEIGHTED.getValue(),
                                "order", 1,
                                "indexCoefficient", 0.5,
                                "greedy", false,
                                "smallWindow", 10,
                                "bigWindow", 20
                        ),
                        "cross EWMA [minimumProfit=0.2, order=1, indexCoefficient=0.5, greedy=false, smallWindow=10, bigWindow=20]"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateStrategy_givesStrategyProperName")
    void createStrategy_givesStrategyProperName(final StrategyType strategyType, final Map<String, Object> params, final String expectedName) {
        final TradingConfig tradingConfig = new TradingConfig()
                .setStrategyType(strategyType)
                .setStrategyParams(params);
        TradingStrategy strategy = factory.createStrategy(tradingConfig);

        Assertions.assertEquals(expectedName, strategy.getName());
    }

    // region ConservativeStrategy strategy creation tests

    @Test
    void createStrategy_throwIllegalArgumentException_whenConservative_andMinimumProfitIsNull() {
        final TradingConfig tradingConfig = new TradingConfig()
                .setStrategyType(StrategyType.CONSERVATIVE)
                .setStrategyParams(Map.of());

        final Executable executable = () -> factory.createStrategy(tradingConfig);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "minimumProfit is mandatory");
    }

    // endregion

    // region CrossStrategy creation tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateStrategy_throwsIllegalArgumentException_whenCross_andParamsAreNotValid() {
        return Stream.of(
                Arguments.of(
                        Map.of(
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", "NotExistingMA",
                                "minimumProfit", 0.1,
                                "order", 1,
                                "indexCoefficient", 0.6f,
                                "greedy", false,
                                "smallWindow", 3,
                                "bigWindow", 6
                        ),
                        "MovingAverageType 'NotExistingMA' not found"
                ),

                Arguments.of(
                        Map.of(
                                "minimumProfit", 0.1,
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
                                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
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
    @MethodSource("getData_forCreateStrategy_throwsIllegalArgumentException_whenCross_andParamsAreNotValid")
    void createStrategy_throwsIllegalArgumentException_whenCross_andParamsAreNotValid(
            final Map<String, Object> params,
            final String expectedMessage
    ) {
        final TradingConfig tradingConfig = new TradingConfig()
                .setStrategyType(StrategyType.CROSS)
                .setStrategyParams(params);

        final Executable executable = () -> factory.createStrategy(tradingConfig);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void createStrategy_throwsNoSuchBeanDefinitionException_whenCross_andNoAveragerBean() {
        final Map<String, Object> params = Map.of(
                "movingAverageType", MovingAverageType.SIMPLE.getValue(),
                "minimumProfit", 0.1,
                "order", 1,
                "indexCoefficient", 0.6f,
                "greedy", false,
                "smallWindow", 3,
                "bigWindow", 6
        );
        final TradingConfig tradingConfig = new TradingConfig()
                .setStrategyType(StrategyType.CROSS)
                .setStrategyParams(params);

        final String averagerName = MovingAverageType.SIMPLE.getAveragerName();
        Mockito.when(applicationContext.getBean(averagerName, MovingAverager.class))
                .thenThrow(new NoSuchBeanDefinitionException("No bean named '" + averagerName + "' available"));
        factory = new TradingStrategyFactory(applicationContext);

        final String expectedMessage = "Averager with movingAverageType 'SMA' not found";

        final Executable executable = () -> factory.createStrategy(tradingConfig);
        Assertions.assertThrows(NoSuchBeanDefinitionException.class, executable, expectedMessage);
    }

    @Test
    void createStrategy_throwsIllegalArgumentException_whenCross_andParamsAreEmpty() {
        final TradingConfig tradingConfig = new TradingConfig()
                .setStrategyType(StrategyType.CROSS)
                .setStrategyParams(Map.of());

        final Executable executable = () -> factory.createStrategy(tradingConfig);
        final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class, executable);

        final String message = exception.getMessage();
        Assertions.assertTrue(message.contains("minimumProfit is mandatory"));
        Assertions.assertTrue(message.contains("smallWindow is mandatory"));
        Assertions.assertTrue(message.contains("bigWindow is mandatory"));
        Assertions.assertTrue(message.contains("indexCoefficient is mandatory"));
        Assertions.assertTrue(message.contains("greedy is mandatory"));
    }

    // endregion

}