package ru.obukhov.trader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.obukhov.trader.web.controller.SandboxController;

@SpringBootTest(args = {"--trading.token=i identify myself as token", "--trading.sandbox=false"})
class ApplicationContextTest {

    @Autowired(required = false)
    private SandboxController sandboxController;

    @Test
    void sandboxControllerNotInitialized_whenSandboxModeIsOff() {
        Assertions.assertNull(sandboxController);
    }

}