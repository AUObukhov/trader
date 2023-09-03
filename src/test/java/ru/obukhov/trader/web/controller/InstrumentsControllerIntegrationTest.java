package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond2;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;
import ru.obukhov.trader.test.utils.model.etf.TestEtf2;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay3;
import ru.obukhov.trader.test.utils.model.share.TestShare2;

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
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/instrument")
                .param("figi", TestInstrument1.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestInstrument1.JSON_STRING);
    }

    @Test
    @SuppressWarnings({"java:S2699", "java:S5976"})
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
        Mocker.mockShare(instrumentsService, TestShare2.TINKOFF_SHARE);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("figi", TestShare2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestShare2.SHARE);
    }

    @Test
    @SuppressWarnings({"java:S2699", "java:S5976"})
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
        Mocker.mockEtf(instrumentsService, TestEtf2.TINKOFF_ETF);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("figi", TestEtf2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestEtf2.JSON_STRING);
    }

    @Test
    @SuppressWarnings({"java:S2699", "java:S5976"})
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
        Mocker.mockBond(instrumentsService, TestBond2.TINKOFF_BOND);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("figi", TestBond2.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestBond2.JSON_STRING);
    }

    @Test
    @SuppressWarnings({"java:S2699", "java:S5976"})
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
        Mocker.mockCurrency(instrumentsService, TestCurrency1.TINKOFF_CURRENCY);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("figi", TestCurrency1.FIGI)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, TestCurrency1.JSON_STRING);
    }

    @Test
    @SuppressWarnings({"java:S2699", "java:S5976"})
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
    void getTradingSchedule_forFuture() throws Exception {
        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);

        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            performAndExpectResponse(requestBuilder, expectedResult);
        }
    }

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule_forFuture_adjustsFromInstant() throws Exception {
        final String exchange = "SPB";

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 1, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3, offset);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            performAndExpectResponse(requestBuilder, expectedResult);
        }
    }

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule_forFuture_adjustsToInstant() throws Exception {
        final String exchange = "MOEX";

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 1, offset);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            performAndExpectResponse(requestBuilder, expectedResult);
        }
    }

    @Test
    @SuppressWarnings("squid:S2699")
        // Tests should include assertions
    void getTradingSchedule_forPast() throws Exception {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.createEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.createDateTime(year, month, 27);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusNanos(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> expectedResult = List.of(
                    TestData.createTradingDay(true, year, month, 18, hour, durationHours),
                    TestData.createTradingDay(false, year, month, 19, hour, durationHours),
                    TestData.createTradingDay(false, year, month, 20, hour, durationHours),
                    TestData.createTradingDay(true, year, month, 21, hour, durationHours),
                    TestData.createTradingDay(true, year, month, 22, hour, durationHours),
                    TestData.createTradingDay(true, year, month, 23, hour, durationHours),
                    TestData.createTradingDay(true, year, month, 24, hour, durationHours),
                    TestData.createTradingDay(true, year, month, 25, hour, durationHours),
                    TestData.createTradingDay(false, year, month, 26, hour, durationHours),
                    TestData.createTradingDay(false, year, month, 27, hour, durationHours)
            );

            performAndExpectResponse(requestBuilder, expectedResult);
        }
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

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule1 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange1)
                .addDays(TestTradingDay1.TINKOFF_TRADING_DAY)
                .addDays(TestTradingDay2.TINKOFF_TRADING_DAY)
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2)
                .addDays(TestTradingDay3.TINKOFF_TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedules")
                .content(TestUtils.OBJECT_MAPPER.writeValueAsString(Interval.of(from, to)))
                .contentType(MediaType.APPLICATION_JSON);

        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, TestTradingDay3.TRADING_DAY);
        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        performAndExpectResponse(requestBuilder, expectedResult);
    }

    private void mockTradingSchedule(final String exchange, final OffsetDateTime from, final OffsetDateTime to) {
        final Instant fromInstant = DateUtils.toSameDayInstant(from);
        final Instant toInstant = DateUtils.toSameDayInstant(to);
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange)
                .addDays(TestTradingDay1.TINKOFF_TRADING_DAY)
                .addDays(TestTradingDay2.TINKOFF_TRADING_DAY)
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)).thenReturn(tradingSchedule);
    }

}