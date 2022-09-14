package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;
import ru.obukhov.trader.test.utils.model.etf.TestEtf2;
import ru.obukhov.trader.test.utils.model.etf.TestEtf3;
import ru.obukhov.trader.test.utils.model.etf.TestEtf4;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.obukhov.trader.test.utils.model.share.TestShare5;

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
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

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
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare4.createTinkoffShare(), TestShare5.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/shares")
                .param("ticker", TestShare4.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<Share> expectedShares = List.of(TestShare4.createShare(), TestShare5.createShare());
        performAndExpectResponse(requestBuilder, expectedShares);
    }

    // endregion

    // region getShare tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShare() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", TestShare2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.createShare());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsShareIgnoreCase() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", TestShare2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.createShare());
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
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(TestShare1.createTinkoffShare(), TestShare2.createTinkoffShare());
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String ticker3 = TestShare3.TICKER;

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker3)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker " + ticker3 + ". Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleShare_returnsServerError_whenMultipleShares() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Share> shares = List.of(
                TestShare1.createTinkoffShare(),
                TestShare4.createTinkoffShare(),
                TestShare5.createTinkoffShare()
        );
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(shares);

        final String ticker = TestShare4.TICKER.toLowerCase();
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("ticker", ticker)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single share for ticker " + ticker + ". Found 2";
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
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf2.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

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
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(
                TestEtf1.createTinkoffEtf(),
                TestEtf3.createTinkoffEtf(),
                TestEtf4.createTinkoffEtf()
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etfs")
                .param("ticker", TestEtf3.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        final List<Etf> expectedEtfs = List.of(TestEtf3.createEtf(), TestEtf4.createEtf());
        performAndExpectResponse(requestBuilder, expectedEtfs);
    }

    // endregion

    // region getSingleEtf tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsEtf() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf2.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf2.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestEtf2.createEtf());
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsEtfIgnoreCase() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf2.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf2.TICKER.toLowerCase())
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestEtf2.createEtf());
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
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(TestEtf1.createTinkoffEtf(), TestEtf2.createTinkoffEtf());
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single etf for ticker " + TestEtf3.TICKER + ". Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getSingleEtf_returnsServerError_whenMultipleEtfs() throws Exception {
        final List<ru.tinkoff.piapi.contract.v1.Etf> etfs = List.of(
                TestEtf1.createTinkoffEtf(),
                TestEtf3.createTinkoffEtf(),
                TestEtf4.createTinkoffEtf()
        );
        Mockito.when(instrumentsService.getAllEtfsSync()).thenReturn(etfs);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("ticker", TestEtf3.TICKER)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single etf for ticker " + TestEtf3.TICKER + ". Found 2";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    // endregion

}