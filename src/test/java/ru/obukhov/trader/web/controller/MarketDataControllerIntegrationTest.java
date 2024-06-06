package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.web.model.exchange.FigiesListRequest;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;
import java.util.stream.Stream;

class MarketDataControllerIntegrationTest extends ControllerIntegrationTest {

    // region getTradingStatus tests

    @Test
    void getTradingStatus_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getTradingStatus_returnsStatus() throws Exception {
        final String figi = TestShares.APPLE.getFigi();

        final SecurityTradingStatus status = SecurityTradingStatus.SECURITY_TRADING_STATUS_OPENING_PERIOD;
        Mocker.mockTradingStatus(marketDataService, figi, status);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .param("figi", figi)
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, status);
    }

    // endregion

    // region convertCurrency tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConvertCurrencyIntoItself() {
        return Stream.of(
                Arguments.of(TestCurrencies.USD),
                Arguments.of(TestCurrencies.RUB)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forConvertCurrencyIntoItself")
    void convertCurrencyIntoItself(final TestCurrency testCurrency) throws Exception {
        final String currencyIsoName = testCurrency.tinkoffCurrency().getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/convert-currency")
                .param("sourceCurrencyIsoName", currencyIsoName)
                .param("targetCurrencyIsoName", currencyIsoName)
                .param("sourceValue", sourceValue.toString())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, sourceValue);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConvertCurrency() {
        return Stream.of(
                Arguments.of(TestCurrencies.USD, TestCurrencies.RUB, 97.31, 1, 97310),
                Arguments.of(TestCurrencies.RUB, TestCurrencies.USD, 1, 97.31, 10.276436132),
                Arguments.of(TestCurrencies.USD, TestCurrencies.CNY, 97.31, 13.322, 7304.458789971),
                Arguments.of(TestCurrencies.CNY, TestCurrencies.USD, 13.322, 97.31, 136.90268215)
        );
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency(
            final TestCurrency sourceTestCurrency,
            final TestCurrency targetTestCurrency,
            final double price1,
            final double price2,
            final double expectedResult
    ) throws Exception {
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = sourceTestCurrency.tinkoffCurrency();
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = targetTestCurrency.tinkoffCurrency();

        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency, targetCurrency);

        final SequencedMap<String, Double> figiesToPrices = new LinkedHashMap<>();
        figiesToPrices.put(sourceCurrency.getFigi(), price1);
        figiesToPrices.put(targetCurrency.getFigi(), price2);
        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/convert-currency")
                .param("sourceCurrencyIsoName", sourceCurrency.getIsoCurrencyName())
                .param("targetCurrencyIsoName", targetCurrency.getIsoCurrencyName())
                .param("sourceValue", "1000")
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, DecimalUtils.setDefaultScale(expectedResult));
    }

    @Test
    @DirtiesContext
    void convertCurrency_returnsBadRequest_whenCurrencyNotFound() throws Exception {
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = TestCurrencies.USD.tinkoffCurrency();
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = TestCurrencies.RUB.tinkoffCurrency();

        Mocker.mockAllCurrencies(instrumentsService, targetCurrency);

        final SequencedMap<String, Double> figiesToPrices = new LinkedHashMap<>(2, 1);
        figiesToPrices.put(targetCurrency.getFigi(), 1.0);

        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/convert-currency")
                .param("sourceCurrencyIsoName", sourceCurrency.getIsoCurrencyName())
                .param("targetCurrencyIsoName", targetCurrency.getIsoCurrencyName())
                .param("sourceValue", "1000")
                .contentType(MediaType.APPLICATION_JSON);

        assertBadRequestResult(requestBuilder, "Instrument not found for id " + sourceCurrency.getIsoCurrencyName());
    }

    // endregion

    @Test
    void getLastPrices_returnsPrices() throws Exception {
        final SequencedMap<String, BigDecimal> figiesToPrices = new LinkedHashMap<>(3, 1);
        figiesToPrices.put(TestShares.APPLE.getFigi(), DecimalUtils.setDefaultScale(175.0));
        figiesToPrices.put(TestShares.SBER.getFigi(), DecimalUtils.setDefaultScale(270.0));
        figiesToPrices.put(TestShares.YANDEX.getFigi(), DecimalUtils.setDefaultScale(2600.0));

        Mocker.mockLastPricesBigDecimal(marketDataService, figiesToPrices);

        final FigiesListRequest request = new FigiesListRequest(figiesToPrices.keySet().stream().toList());
        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/last-prices")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, figiesToPrices);
    }

    @Test
    void getLastPrices_returnsBadRequest_whenInstrumentNotFound() throws Exception {
        final String figi1 = TestShares.APPLE.getFigi();
        final String figi2 = TestShares.SBER.getFigi();
        final String figi3 = TestShares.YANDEX.getFigi();

        final List<String> figies = List.of(figi1, figi2, figi3);
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(figi1, 175.0),
                TestData.newLastPrice(figi3, 2600.0)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final FigiesListRequest request = new FigiesListRequest(figies);
        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/last-prices")
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);

        assertBadRequestResult(requestBuilder, "Instrument not found for id " + figi2);
    }

}