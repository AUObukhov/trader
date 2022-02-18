package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.Application;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.SandboxSetCurrencyBalanceRequest;
import ru.obukhov.trader.market.model.SandboxSetPositionBalanceRequest;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.obukhov.trader.web.client.service.interfaces.MarketClient;
import ru.obukhov.trader.web.client.service.interfaces.SandboxClient;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--trading.token=i identify myself as token", "--trading.sandbox=true"}
)
class SandboxControllerWebTest extends ControllerWebTest {

    @MockBean
    private SandboxClient sandboxClient;
    @MockBean
    private MarketClient marketClient;

    @Test
    void setCurrencyBalance_setsBalance() throws Exception {
        final String request = ResourceUtils.getTestDataAsString("SetCurrencyBalanceRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/currency-balance")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        final SandboxSetCurrencyBalanceRequest expectedRequest = new SandboxSetCurrencyBalanceRequest();
        expectedRequest.setBalance(BigDecimal.valueOf(100000));
        expectedRequest.setCurrency(Currency.USD);
        Mockito.verify(sandboxClient, Mockito.times(1))
                .setCurrencyBalance(expectedRequest, "brokerAccountId");
    }

    @Test
    void setPositionBalance_setsBalance() throws Exception {
        final String figi = "Figi";

        final String request = ResourceUtils.getTestDataAsString("SetPositionBalanceRequest.json");

        final MarketInstrument instrument = new MarketInstrument().figi(figi);
        Mockito.when(marketClient.searchMarketInstrumentsByTicker("ticker")).thenReturn(List.of(instrument));

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/position-balance")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        final SandboxSetPositionBalanceRequest expectedRequest = new SandboxSetPositionBalanceRequest();
        expectedRequest.setBalance(BigDecimal.valueOf(100000));
        expectedRequest.setFigi(figi);
        Mockito.verify(sandboxClient, Mockito.times(1))
                .setPositionBalance(expectedRequest, "brokerAccountId");
    }

    @Test
    void clearAll_clears() throws Exception {
        final String request = ResourceUtils.getTestDataAsString("ClearAllRequest.json");

        mockMvc.perform(MockMvcRequestBuilders.post("/trader/sandbox/clear")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(sandboxClient, Mockito.times(1))
                .clearAll("brokerAccountId");
    }

}