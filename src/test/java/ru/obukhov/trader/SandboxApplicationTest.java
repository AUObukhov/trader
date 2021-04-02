package ru.obukhov.trader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.obukhov.trader.web.controller.SandboxController;
import ru.tinkoff.invest.openapi.SandboxContext;
import ru.tinkoff.invest.openapi.okhttp.InterceptingOpenApi;

@SpringBootTest(args = {"--trading.token=i identify myself as token", "--trading.sandbox=true"})
class SandboxApplicationTest extends BaseMockedTest {

    @Autowired
    private SandboxController sandboxController;

    @MockBean
    private InterceptingOpenApi openApi;

    @Mock
    private SandboxContext sandboxContext;

    @Test
    void sandboxControllerNotInitialized_whenSandboxModeIsOn() {
        Assertions.assertNotNull(sandboxController);
    }

}