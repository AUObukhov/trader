package ru.obukhov.trader.common.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import ru.obukhov.trader.market.model.MovingAverageType;

import java.util.stream.Stream;

@SpringBootTest(args = {"--trading.token=i identify myself as token", "--trading.sandbox=false"})
class MovingAveragerIntegrationTest {

    @Test
    void getByType_returnsAveragerForAllTypes() {
        for (MovingAverageType type : MovingAverageType.values()) {
            MovingAverager averager = MovingAverager.getByType(type);

            Assertions.assertNotNull(averager);
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetByType_returnsProperAverager() {
        return Stream.of(
                Arguments.of(SimpleMovingAverager.class, MovingAverageType.SIMPLE),
                Arguments.of(LinearMovingAverager.class, MovingAverageType.LINEAR_WEIGHTED),
                Arguments.of(ExponentialMovingAverager.class, MovingAverageType.EXPONENTIAL_WEIGHTED)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetByType_returnsProperAverager")
    void getByType_returnsProperAverager(Class<? extends MovingAverager> clazz, MovingAverageType type) {
        MovingAverager averager = MovingAverager.getByType(type);

        Assertions.assertEquals(clazz, averager.getClass());
    }

}