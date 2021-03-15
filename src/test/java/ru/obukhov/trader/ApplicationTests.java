package ru.obukhov.trader;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(args = "--trading.token=i identify myself as token")
class ApplicationTests {

    @Test
    @SuppressWarnings("squid:S2699")
    void contextLoads() {
    }

}