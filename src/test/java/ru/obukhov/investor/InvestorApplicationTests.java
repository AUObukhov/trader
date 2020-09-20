package ru.obukhov.investor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.obukhov.investor.service.interfaces.ConnectionService;

@SpringBootTest(args = "i identify myself as token")
class InvestorApplicationTests {

    @MockBean
    private ConnectionService connectionService;

    @Test
    @SuppressWarnings("squid:S2699")
    void contextLoads() {
    }

}