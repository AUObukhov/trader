package ru.obukhov.trader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.web.controller.SandboxController;
import ru.tinkoff.invest.openapi.okhttp.InterceptingOpenApi;
import ru.tinkoff.invest.openapi.okhttp.SandboxContext;

@ActiveProfiles("test")
@SpringBootTest(args = {"--trading.token=i identify myself as token", "--trading.sandbox=true"})
@ExtendWith(MockitoExtension.class)
class ApplicationSandboxContextTest {

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