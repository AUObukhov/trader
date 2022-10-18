package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond1;
import ru.obukhov.trader.test.utils.model.bond.TestBond2;
import ru.obukhov.trader.test.utils.model.bond.TestBond3;
import ru.obukhov.trader.test.utils.model.bond.TestBond4;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency2;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency3;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency4;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;
import ru.obukhov.trader.test.utils.model.etf.TestEtf2;
import ru.obukhov.trader.test.utils.model.etf.TestEtf3;
import ru.obukhov.trader.test.utils.model.etf.TestEtf4;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay3;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.obukhov.trader.test.utils.model.share.TestShare5;

import java.time.OffsetDateTime;
import java.util.List;

class InstrumentsControllerIntegrationTest extends ControllerIntegrationTest {

    // region getShares tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsEmptyResponse_whenNoShares() throws Exception {
        Mocker.mockShares(instrumentsService, TestShare1.TINKOFF_SHARE, TestShare2.TINKOFF_SHARE);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", TestShare3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShares_returnsMultipleShares_whenMultipleShares() throws Exception {
        Mocker.mockShares(instrumentsService, TestShare4.TINKOFF_SHARE, TestShare5.TINKOFF_SHARE);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", TestShare4.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<Share> expectedShares = List.of(TestShare4.SHARE, TestShare5.SHARE);
        performAndExpectResponse(requestBuilder, expectedShares);
    }

    // endregion

    // region getShare tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShare() throws Exception {
        Mocker.mockShares(instrumentsService, TestShare1.TINKOFF_SHARE, TestShare2.TINKOFF_SHARE);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", TestShare2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.SHARE);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShareIgnoreCase() throws Exception {
        Mocker.mockShares(instrumentsService, TestShare1.TINKOFF_SHARE, TestShare2.TINKOFF_SHARE);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", TestShare2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.SHARE);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsBadRequest_whenNoShare() throws Exception {
        Mocker.mockShares(instrumentsService, TestShare1.TINKOFF_SHARE, TestShare2.TINKOFF_SHARE);

        final String ticker3 = TestShare3.TICKER;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker3)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker '" + ticker3 + "'. Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsServerError_whenMultipleShares() throws Exception {
        Mocker.mockShares(instrumentsService, TestShare1.TINKOFF_SHARE, TestShare4.TINKOFF_SHARE, TestShare5.TINKOFF_SHARE);

        final String ticker = TestShare4.TICKER.toLowerCase();
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker '" + ticker + "'. Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

    // region getEtfs tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtfs_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtfs_returnsEmptyResponse_whenNoEtfs() throws Exception {
        Mocker.mockEtfs(instrumentsService, TestEtf1.TINKOFF_ETF, TestEtf2.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .param("ticker", TestEtf3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtfs_returnsMultipleEtfs_whenMultipleEtfs() throws Exception {
        Mocker.mockEtfs(instrumentsService, TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF, TestEtf4.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .param("ticker", TestEtf3.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<Etf> expectedEtfs = List.of(TestEtf3.ETF, TestEtf4.ETF);
        performAndExpectResponse(requestBuilder, expectedEtfs);
    }

    // endregion

    // region getSingleEtf tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsEtf() throws Exception {
        Mocker.mockEtfs(instrumentsService, TestEtf1.TINKOFF_ETF, TestEtf2.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestEtf2.ETF);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsEtfIgnoreCase() throws Exception {
        Mocker.mockEtfs(instrumentsService, TestEtf1.TINKOFF_ETF, TestEtf2.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestEtf2.ETF);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsServerError_whenNoEtf() throws Exception {
        Mocker.mockEtfs(instrumentsService, TestEtf1.TINKOFF_ETF, TestEtf2.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single etf for ticker '" + TestEtf3.TICKER + "'. Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsServerError_whenMultipleEtfs() throws Exception {
        Mocker.mockEtfs(instrumentsService, TestEtf1.TINKOFF_ETF, TestEtf3.TINKOFF_ETF, TestEtf4.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single etf for ticker '" + TestEtf3.TICKER + "'. Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

    // region getBonds tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getBonds_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bonds")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getBonds_returnsEmptyResponse_whenNoBonds() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bonds")
                .param("ticker", TestBond2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getBonds_returnsMultipleBonds_whenMultipleBonds() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bonds")
                .param("ticker", TestBond3.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<Bond> expectedBonds = List.of(TestBond3.BOND, TestBond4.BOND);
        performAndExpectResponse(requestBuilder, expectedBonds);
    }

    // endregion

    // region getSingleBond tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleBond_returnsBond() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("ticker", TestBond2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestBond2.BOND);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleBond_returnsBondIgnoreCase() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond2.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("ticker", TestBond2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestBond2.BOND);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleBond_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleBond_returnsServerError_whenNoBond() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("ticker", TestBond2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single bond for ticker '" + TestBond2.TICKER + "'. Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleBond_returnsServerError_whenMultipleBonds() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Bond> bonds = List.of(
                TestBond1.TINKOFF_BOND,
                TestBond3.TINKOFF_BOND,
                TestBond4.TINKOFF_BOND
        );
        Mockito.when(instrumentsService.getAllBondsSync()).thenReturn(bonds);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("ticker", TestBond3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single bond for ticker '" + TestBond3.TICKER + "'. Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

    // region getCurrencies tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCurrencies_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currencies")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCurrencies_returnsEmptyResponse_whenNoCurrencies() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currencies")
                .param("ticker", TestCurrency2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, List.of());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCurrencies_returnsMultipleCurrencies_whenMultipleCurrencies() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currencies")
                .param("ticker", TestCurrency3.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<CurrencyInstrument> expectedCurrencies = List.of(TestCurrency3.CURRENCY, TestCurrency4.CURRENCY);
        performAndExpectResponse(requestBuilder, expectedCurrencies);
    }

    // endregion

    // region getSingleBond tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleCurrency_returnsCurrency() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("ticker", TestCurrency2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestCurrency2.CURRENCY);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleCurrency_returnsCurrencyIgnoreCase() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency2.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("ticker", TestCurrency2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestCurrency2.CURRENCY);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleCurrency_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleCurrency_returnsServerError_whenNoCurrency() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("ticker", TestCurrency2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single currency for ticker '" + TestCurrency2.TICKER + "'. Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleCurrency_returnsServerError_whenMultipleCurrencies() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Currency> currencies = List.of(
                TestCurrency1.TINKOFF_CURRENCY,
                TestCurrency3.TINKOFF_CURRENCY,
                TestCurrency4.TINKOFF_CURRENCY
        );
        Mockito.when(instrumentsService.getAllCurrenciesSync()).thenReturn(currencies);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("ticker", TestCurrency3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single currency for ticker '" + TestCurrency3.TICKER + "'. Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

    // region getTradingSchedule tests

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule() throws Exception {
        final Exchange exchange = Exchange.MOEX;
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3);

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange.getValue())
                .addDays(TestTradingDay1.TINKOFF_TRADING_DAY)
                .addDays(TestTradingDay2.TINKOFF_TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange.getValue(), from.toInstant(), to.toInstant())).thenReturn(tradingSchedule);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange.getValue())
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    // endregion

    // region getTradingSchedules tests

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedules() throws Exception {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 8, 3);

        final Exchange exchange1 = Exchange.MOEX;
        final Exchange exchange2 = Exchange.SPB;

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule1 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange1.getValue())
                .addDays(TestTradingDay1.TINKOFF_TRADING_DAY)
                .addDays(TestTradingDay2.TINKOFF_TRADING_DAY)
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2.getValue())
                .addDays(TestTradingDay3.TINKOFF_TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedules")
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedTradingDays1 = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);
        final List<TradingDay> expectedTradingDays2 = List.of(TestTradingDay3.TRADING_DAY);
        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, expectedTradingDays1);
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, expectedTradingDays2);
        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    // endregion

}