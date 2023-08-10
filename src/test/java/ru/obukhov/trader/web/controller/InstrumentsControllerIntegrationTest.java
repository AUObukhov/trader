package ru.obukhov.trader.web.controller;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond2;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency2;
import ru.obukhov.trader.test.utils.model.etf.TestEtf2;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay3;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.contract.v1.TradingSchedule;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

class InstrumentsControllerIntegrationTest extends ControllerIntegrationTest {

    // region getInstrument tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstrument_returnsInstrument() throws Exception {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.INSTRUMENT);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/instrument")
                .param("figi", TestInstrument1.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestInstrument1.INSTRUMENT);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstrument_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/instrument")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getShare tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShare_returnsShare() throws Exception {
        Mocker.mockShare(instrumentsService, TestShare2.SHARE);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("figi", TestShare2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.SHARE);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getShare_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getEtf tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtf_returnsEtf() throws Exception {
        Mocker.mockEtf(instrumentsService, TestEtf2.ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("figi", TestEtf2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestEtf2.STRING);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getEtf_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getBond tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getBond_returnsBond() throws Exception {
        Mocker.mockBond(instrumentsService, TestBond2.BOND);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("figi", TestBond2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestBond2.STRING);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getBond_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getCurrency tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCurrency_returnsCurrency() throws Exception {
        Mocker.mockCurrency(instrumentsService, TestCurrency2.CURRENCY);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("figi", TestCurrency2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestCurrency2.CURRENCY);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getCurrency_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule() throws Exception {
        final String exchange = "MOEX";
        final Timestamp from = TimestampUtils.newTimestamp(2022, 10, 3, 3);
        final Timestamp to = TimestampUtils.newTimestamp(2022, 10, 7, 3);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule_adjustsFromInstant() throws Exception {
        final String exchange = "SPB";

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final Timestamp from = TimestampUtils.newTimestamp(2022, 10, 3, 1, offset);
        final Timestamp to = TimestampUtils.newTimestamp(2022, 10, 7, 3, offset);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule_adjustsToInstant() throws Exception {
        final String exchange = "MOEX";

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final Timestamp from = TimestampUtils.newTimestamp(2022, 10, 3, 3, offset);
        final Timestamp to = TimestampUtils.newTimestamp(2022, 10, 7, 1, offset);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
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
        final String exchange1 = "MOEX";
        final String exchange2 = "SPB";

        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 8, 3);

        final TradingSchedule tradingSchedule1 = TradingSchedule.newBuilder()
                .setExchange(exchange1)
                .addDays(TestTradingDay1.TRADING_DAY)
                .addDays(TestTradingDay2.TRADING_DAY)
                .build();
        final TradingSchedule tradingSchedule2 = TradingSchedule.newBuilder()
                .setExchange(exchange2)
                .addDays(TestTradingDay3.TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedules")
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingSchedule> expectedResult = List.of(tradingSchedule1, tradingSchedule2);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    private void mockTradingSchedule(final String exchange, final Timestamp from, final Timestamp to) {
        final Instant fromInstant = TimestampUtils.toStartOfDayInstant(from);
        final Instant toInstant = TimestampUtils.toStartOfDayInstant(to);
        final TradingSchedule tradingSchedule = TradingSchedule.newBuilder()
                .setExchange(exchange)
                .addDays(TestTradingDay1.TRADING_DAY)
                .addDays(TestTradingDay2.TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)).thenReturn(tradingSchedule);
    }

}