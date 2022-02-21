package ru.obukhov.trader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.web.client.service.interfaces.SandboxClient;
import ru.obukhov.trader.web.controller.SandboxController;

@ActiveProfiles("test")
@SpringBootTest(args = {"--trading.token=i identify myself as token", "--trading.sandbox=true"})
@ExtendWith(MockitoExtension.class)
class ApplicationSandboxClientTest {

    @Autowired
    private SandboxController sandboxController;

    @MockBean
    private SandboxClient sandboxClient;
    @MockBean
    private TokenValidationStartupListener tokenValidationStartupListener;

    @Test
    void sandboxControllerNotInitialized_whenSandboxModeIsOn() {
        Assertions.assertNotNull(sandboxController);
    }

}