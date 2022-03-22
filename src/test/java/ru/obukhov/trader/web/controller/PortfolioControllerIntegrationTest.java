package ru.obukhov.trader.web.controller;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockserver.model.HttpRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.CurrencyPosition;
import ru.obukhov.trader.market.model.Portfolio;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.exchange.PortfolioCurrenciesResponse;
import ru.obukhov.trader.web.client.exchange.PortfolioResponse;
import ru.obukhov.trader.web.model.exchange.GetPortfolioCurrenciesResponse;
import ru.obukhov.trader.web.model.exchange.GetPortfolioPositionsResponse;

import java.math.BigDecimal;
import java.util.List;

class PortfolioControllerIntegrationTest extends ControllerIntegrationTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getPositions_returnsPositions(@Nullable final String brokerAccountId) throws Exception {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/portfolio");

        final PortfolioPosition position1 = new PortfolioPosition()
                .setTicker("ticker1")
                .setBalance(BigDecimal.valueOf(10000))
                .setExpectedYield(TestData.createMoneyAmount(Currency.RUB, 1000))
                .setCount(5)
                .setAveragePositionPrice(TestData.createMoneyAmount(Currency.RUB, 2000))
                .setAveragePositionPriceNoNkd(TestData.createMoneyAmount(Currency.RUB, 2000))
                .setName("name1");

        final PortfolioPosition position2 = new PortfolioPosition()
                .setTicker("ticker2")
                .setBalance(BigDecimal.valueOf(20000))
                .setExpectedYield(TestData.createMoneyAmount(Currency.USD, 2000))
                .setCount(5)
                .setAveragePositionPrice(TestData.createMoneyAmount(Currency.USD, 4000))
                .setAveragePositionPriceNoNkd(TestData.createMoneyAmount(Currency.USD, 4000))
                .setName("name2");

        final List<PortfolioPosition> positions = List.of(position1, position2);

        final PortfolioResponse portfolioResponse = new PortfolioResponse();
        final Portfolio portfolio = new Portfolio();
        portfolio.setPositions(positions);
        portfolioResponse.setPayload(portfolio);
        mockResponse(apiRequest, portfolioResponse);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/positions")
                .param("brokerAccountId", brokerAccountId)
                .contentType(MediaType.APPLICATION_JSON);
        final String expectedResponse = objectMapper.writeValueAsString(new GetPortfolioPositionsResponse(positions));
        performAndVerifyResponse(requestBuilder, expectedResponse);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getCurrencies_returnsCurrencies(@Nullable final String brokerAccountId) throws Exception {
        final HttpRequest apiRequest = createAuthorizedHttpRequest(HttpMethod.GET)
                .withPath("/openapi/portfolio/currencies");

        final CurrencyPosition currencyPosition1 = new CurrencyPosition()
                .currency(Currency.RUB)
                .balance(BigDecimal.valueOf(10000))
                .blocked(BigDecimal.ZERO);
        final CurrencyPosition currencyPosition2 = new CurrencyPosition()
                .currency(Currency.EUR)
                .balance(BigDecimal.valueOf(1000))
                .blocked(BigDecimal.valueOf(100));

        final List<CurrencyPosition> currencies = List.of(currencyPosition1, currencyPosition2);

        final PortfolioCurrenciesResponse portfolioResponse = new PortfolioCurrenciesResponse();
        portfolioResponse.setCurrencies(currencies);
        mockResponse(apiRequest, portfolioResponse);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/portfolio/currencies")
                .param("brokerAccountId", brokerAccountId)
                .contentType(MediaType.APPLICATION_JSON);
        final String expectedResponse = objectMapper.writeValueAsString(new GetPortfolioCurrenciesResponse(currencies));
        performAndVerifyResponse(requestBuilder, expectedResponse);
    }

}