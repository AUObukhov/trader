package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.market.interfaces.PortfolioService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.Arrays;

class PortfolioControllerWebTest extends ControllerIntegrationTest {

    @MockBean
    private PortfolioService portfolioService;

    @Test
    void getPositions() throws Exception {
        PortfolioPosition position1 = new PortfolioPosition(
                "ticker1",
                BigDecimal.valueOf(10000),
                null,
                Currency.RUB,
                BigDecimal.valueOf(1000),
                5,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(2000),
                "name1"
        );

        PortfolioPosition position2 = new PortfolioPosition(
                "ticker2",
                BigDecimal.valueOf(20000),
                null,
                Currency.USD,
                BigDecimal.valueOf(2000),
                5,
                BigDecimal.valueOf(4000),
                BigDecimal.valueOf(4000),
                "name2"
        );

        Mockito.when(portfolioService.getPositions()).thenReturn(Arrays.asList(position1, position2));

        String expectedResponse = ResourceUtils.getResourceAsString("test-data/GetPortfolioPositionsResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/portfolio/positions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(portfolioService, Mockito.times(1)).getPositions();
    }

    @Test
    void getCurrencies() throws Exception {
        CurrencyPosition currencyPosition1 = new CurrencyPosition()
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(10000))
                .blocked(BigDecimal.ZERO);
        CurrencyPosition currencyPosition2 = new CurrencyPosition()
                .currency(Currency.EUR)
                .balance(BigDecimal.valueOf(1000))
                .blocked(BigDecimal.valueOf(100));

        Mockito.when(portfolioService.getCurrencies()).thenReturn(Arrays.asList(currencyPosition1, currencyPosition2));

        String expectedResponse =
                ResourceUtils.getResourceAsString("test-data/getPortfolioCurrenciesResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/portfolio/currencies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(portfolioService, Mockito.times(1)).getCurrencies();
    }

}