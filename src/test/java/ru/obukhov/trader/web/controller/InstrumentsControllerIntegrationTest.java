package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond;
import ru.obukhov.trader.test.utils.model.bond.TestBonds;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.etf.TestEtf;
import ru.obukhov.trader.test.utils.model.etf.TestEtfs;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDay;
import ru.obukhov.trader.test.utils.model.trading_day.TestTradingDays;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

class InstrumentsControllerIntegrationTest extends ControllerIntegrationTest {

    // region getInstrument tests

    @Test
    void getInstrument_returnsInstrument() throws Exception {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/instrument")
                .param("figi", testInstrument.instrument().figi())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, testInstrument.jsonString());
    }

    @Test
    @SuppressWarnings("java:S5976")
        // Similar tests should be grouped in a single Parameterized test
    void getInstrument_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/instrument")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getShare tests

    @Test
    void getShare_returnsShare() throws Exception {
        final TestShare testShare = TestShares.SBER;
        Mocker.mockShare(instrumentsService, testShare.tinkoffShare());

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .param("figi", testShare.share().figi())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, testShare.share());
    }

    @Test
    @SuppressWarnings("java:S5976")
        // Similar tests should be grouped in a single Parameterized test
    void getShare_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/share")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getEtf tests

    @Test
    void getEtf_returnsEtf() throws Exception {
        final TestEtf testEtf = TestEtfs.FXUS;

        Mocker.mockEtf(instrumentsService, testEtf.tinkoffEtf());

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .param("figi", testEtf.etf().figi())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, testEtf.jsonString());
    }

    @Test
    @SuppressWarnings("java:S5976")
        // Similar tests should be grouped in a single Parameterized test
    void getEtf_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/etf")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getBond tests

    @Test
    void getBond_returnsBond() throws Exception {
        final TestBond testBond = TestBonds.KAZAKHSTAN;

        Mocker.mockBond(instrumentsService, testBond.tinkoffBond());

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .param("figi", testBond.tinkoffBond().getFigi())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, testBond.jsonString());
    }

    @Test
    @SuppressWarnings("java:S5976")
        // Similar tests should be grouped in a single Parameterized test
    void getBond_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/bond")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getCurrencyByFigi tests

    @Test
    void getCurrency_returnsCurrency() throws Exception {
        final TestCurrency testCurrency = TestCurrencies.USD;

        Mocker.mockCurrency(instrumentsService, testCurrency.tinkoffCurrency());

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .param("figi", testCurrency.currency().figi())
                .contentType(MediaType.APPLICATION_JSON);

        assertResponse(requestBuilder, testCurrency.jsonString());
    }

    @Test
    @SuppressWarnings("java:S5976")
        // Similar tests should be grouped in a single Parameterized test
    void getCurrency_returnsBadRequest_whenFigiIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/currency")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'figi' for method parameter type String is not present";
        assertBadRequestResult(requestBuilder, expectedMessage);
    }

    // endregion

    // region getTradingSchedule tests

    @Test
    void getTradingSchedule_forFuture() throws Exception {
        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 7, 3);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .contentType(MediaType.APPLICATION_JSON);

        final List<TradingDay> expectedResult = List.of(TestTradingDays.TRADING_DAY1.tradingDay(), TestTradingDays.TRADING_DAY2.tradingDay());

        final OffsetDateTime mockedNow = DateUtils.toStartOfDay(from);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            assertResponse(requestBuilder, expectedResult);
        }
    }

    @Test
    void getTradingSchedule_forPast() throws Exception {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);

        mockTradingSchedule(exchange, from, to);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedule")
                .param("exchange", exchange)
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .contentType(MediaType.APPLICATION_JSON);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusNanos(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> expectedResult = List.of(
                    TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                    TestData.newTradingDay(false, year, month, 19, hour, durationHours),
                    TestData.newTradingDay(false, year, month, 20, hour, durationHours),
                    TestData.newTradingDay(true, year, month, 21, hour, durationHours),
                    TestData.newTradingDay(true, year, month, 22, hour, durationHours),
                    TestData.newTradingDay(true, year, month, 23, hour, durationHours),
                    TestData.newTradingDay(true, year, month, 24, hour, durationHours),
                    TestData.newTradingDay(true, year, month, 25, hour, durationHours),
                    TestData.newTradingDay(false, year, month, 26, hour, durationHours)
            );

            assertResponse(requestBuilder, expectedResult);
        }
    }

    // endregion

    // region getTradingSchedules tests

    @Test
    void getTradingSchedules() throws Exception {
        final String exchange1 = "MOEX";
        final String exchange2 = "SPB";

        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 8, 3);

        final TestTradingDay testTradingDay1 = TestTradingDays.TRADING_DAY1;
        final TestTradingDay testTradingDay2 = TestTradingDays.TRADING_DAY2;
        final TestTradingDay testTradingDay3 = TestTradingDays.TRADING_DAY3;

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule1 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange1)
                .addDays(testTradingDay1.tinkoffTradingDay())
                .addDays(testTradingDay2.tinkoffTradingDay())
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2)
                .addDays(testTradingDay3.tinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingSchedulesSync(from.toInstant(), to.toInstant()))
                .thenReturn(List.of(tradingSchedule1, tradingSchedule2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/instruments/trading-schedules")
                .param("from", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(from))
                .param("to", DateUtils.OFFSET_DATE_TIME_FORMATTER.format(to))
                .contentType(MediaType.APPLICATION_JSON);

        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, testTradingDay1.tradingDay(), testTradingDay2.tradingDay());
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, testTradingDay3.tradingDay());
        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        assertResponse(requestBuilder, expectedResult);
    }

    private void mockTradingSchedule(final String exchange, final OffsetDateTime from, final OffsetDateTime to) {
        final Instant fromInstant = DateUtils.toSameDayInstant(from);
        final Instant toInstant = DateUtils.toSameDayInstant(to);
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange)
                .addDays(TestTradingDays.TRADING_DAY1.tinkoffTradingDay())
                .addDays(TestTradingDays.TRADING_DAY2.tinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)).thenReturn(tradingSchedule);
    }

}