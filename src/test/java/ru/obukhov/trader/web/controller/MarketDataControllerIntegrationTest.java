package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency2;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency3;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
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
        final String figi = TestShare1.FIGI;

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
    static Stream<Arguments> getData_forConvertCurrencyToItself() {
        return Stream.of(
                Arguments.of(TestCurrency1.TINKOFF_CURRENCY, 97.31),
                Arguments.of(TestCurrency2.TINKOFF_CURRENCY, 1)
        );
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrencyToItself")
    void convertCurrencyToItself(final ru.tinkoff.piapi.contract.v1.Currency currency, final double price) throws Exception {
        Mocker.mockAllCurrencies(instrumentsService, currency);

        final Map<String, Double> figiesToPrices = Map.of(currency.getFigi(), price);
        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/convert-currency")
                .param("sourceCurrencyIsoName", currency.getIsoCurrencyName())
                .param("targetCurrencyIsoName", currency.getIsoCurrencyName())
                .param("sourceValue", sourceValue.toString())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, sourceValue);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConvertCurrency() {
        return Stream.of(
                Arguments.of(TestCurrency1.TINKOFF_CURRENCY, TestCurrency2.TINKOFF_CURRENCY, 97.31, 1, 97310),
                Arguments.of(TestCurrency2.TINKOFF_CURRENCY, TestCurrency1.TINKOFF_CURRENCY, 1, 97.31, 10.276436132),
                Arguments.of(TestCurrency1.TINKOFF_CURRENCY, TestCurrency3.TINKOFF_CURRENCY, 97.31, 13.322, 7304.458789971),
                Arguments.of(TestCurrency3.TINKOFF_CURRENCY, TestCurrency1.TINKOFF_CURRENCY, 13.322, 97.31, 136.90268215)
        );
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency(
            final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency,
            final ru.tinkoff.piapi.contract.v1.Currency targetCurrency,
            final double price1,
            final double price2,
            final double expectedResult
    ) throws Exception {
        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency, targetCurrency);

        final Map<String, Double> figiesToPrices = new LinkedHashMap<>();
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
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = TestCurrency1.TINKOFF_CURRENCY;
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = TestCurrency2.TINKOFF_CURRENCY;

        Mocker.mockAllCurrencies(instrumentsService, targetCurrency);

        final Map<String, Double> figiesToPrices = new LinkedHashMap<>(2, 1);
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

}