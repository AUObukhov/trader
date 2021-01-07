package ru.obukhov.investor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(args = "--trading.token=i identify myself as token")
class InvestorApplicationTests {

    @Test
    @SuppressWarnings("squid:S2699")
    void contextLoads() {
    }

}