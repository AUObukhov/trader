package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.market.impl.PortfolioService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.math.BigDecimal;
import java.util.List;

class PortfolioControllerWebTest extends ControllerWebTest {

    @MockBean
    private PortfolioService portfolioService;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPositions(@Nullable final String brokerAccountId) throws Exception {
        final PortfolioPosition position1 = new PortfolioPosition(
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

        final PortfolioPosition position2 = new PortfolioPosition(
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

        Mockito.when(portfolioService.getPositions(brokerAccountId)).thenReturn(List.of(position1, position2));

        final String expectedResponse = ResourceUtils.getTestDataAsString("GetPortfolioPositionsResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/portfolio/positions")
                        .param("brokerAccountId", brokerAccountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(portfolioService, Mockito.times(1)).getPositions(brokerAccountId);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getCurrencies(@Nullable final String brokerAccountId) throws Exception {
        final CurrencyPosition currencyPosition1 = new CurrencyPosition()
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(10000))
                .blocked(BigDecimal.ZERO);
        final CurrencyPosition currencyPosition2 = new CurrencyPosition()
                .currency(Currency.EUR)
                .balance(BigDecimal.valueOf(1000))
                .blocked(BigDecimal.valueOf(100));

        Mockito.when(portfolioService.getCurrencies(brokerAccountId)).thenReturn(List.of(currencyPosition1, currencyPosition2));

        final String expectedResponse = ResourceUtils.getTestDataAsString("getPortfolioCurrenciesResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/portfolio/currencies")
                        .param("brokerAccountId", brokerAccountId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(portfolioService, Mockito.times(1)).getCurrencies(brokerAccountId);
    }

}