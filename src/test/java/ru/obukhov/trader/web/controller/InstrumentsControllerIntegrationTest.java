package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
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
                .addDays(TestTradingDay1.createTinkoffTradingDay())
                .addDays(TestTradingDay2.createTinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange.getValue(), from.toInstant(), to.toInstant())).thenReturn(tradingSchedule);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange.getValue())
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.createTradingDay(), TestTradingDay2.createTradingDay());

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
                .addDays(TestTradingDay1.createTinkoffTradingDay())
                .addDays(TestTradingDay2.createTinkoffTradingDay())
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2.getValue())
                .addDays(TestTradingDay3.createTinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedules")
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedTradingDays1 = List.of(TestTradingDay1.createTradingDay(), TestTradingDay2.createTradingDay());
        final List<TradingDay> expectedTradingDays2 = List.of(TestTradingDay3.createTradingDay());
        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, expectedTradingDays1);
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, expectedTradingDays2);
        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    // endregion

}