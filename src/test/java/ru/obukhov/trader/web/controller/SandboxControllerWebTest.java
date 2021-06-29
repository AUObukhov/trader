package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.Application;
import ru.obukhov.trader.market.interfaces.SandboxService;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.tinkoff.invest.openapi.model.rest.SandboxCurrency;
import ru.tinkoff.invest.openapi.okhttp.InterceptingOpenApi;

import java.math.BigDecimal;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--trading.token=i identify myself as token", "--trading.sandbox=true"}
)
class SandboxControllerWebTest extends ControllerWebTest {

    @MockBean
    private SandboxService sandboxService;
    @MockBean
    private InterceptingOpenApi openApi; // to prevent registration on application start

    @Test
    void setCurrencyBalance_setsBalance() throws Exception {
        final String request = ResourceUtils.getTestDataAsString("SetCurrencyBalanceRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/currency-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(sandboxService, Mockito.times(1))
                .setCurrencyBalance(SandboxCurrency.USD, BigDecimal.valueOf(100000), "brokerAccountId");
    }

    @Test
    void setPositionBalance_setsBalance() throws Exception {
        final String request = ResourceUtils.getTestDataAsString("SetPositionBalanceRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/position-balance")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(sandboxService, Mockito.times(1))
                .setPositionBalance("ticker", BigDecimal.valueOf(100000), "brokerAccountId");
    }

    @Test
    void clearAll_clears() throws Exception {
        final String request = ResourceUtils.getTestDataAsString("ClearAllRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/clear")
                .content(request)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(sandboxService, Mockito.times(1))
                .clearAll("brokerAccountId");
    }

}