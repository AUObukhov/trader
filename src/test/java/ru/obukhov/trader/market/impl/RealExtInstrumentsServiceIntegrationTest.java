package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond2;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency1;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency2;
import ru.obukhov.trader.test.utils.model.etf.TestEtf3;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay1;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay2;
import ru.obukhov.trader.test.utils.model.schedule.TestTradingDay3;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
class RealExtInstrumentsServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private RealExtInstrumentsService realExtInstrumentsService;

    // region getTickerByFigi tests

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsTicker_whenInstrumentFound() {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;

        Mocker.mockTickerByFigi(instrumentsService, ticker, figi);
        final String result = realExtInstrumentsService.getTickerByFigi(figi);

        Assertions.assertEquals(ticker, result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsCachedValue() {
        final String ticker = TestShare1.TICKER;
        final String figi = TestShare1.FIGI;

        Mocker.mockTickerByFigi(instrumentsService, ticker, figi);
        realExtInstrumentsService.getTickerByFigi(figi);

        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(null);
        final String result = realExtInstrumentsService.getTickerByFigi(figi);

        Assertions.assertEquals(ticker, result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_throwsIllegalArgumentException_whenNoInstrument() {
        final String figi = TestShare1.FIGI;

        final Executable executable = () -> realExtInstrumentsService.getTickerByFigi(figi);
        final String expectedMessage = "Not found instrument for FIGI '" + figi + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getExchange tests

    @Test
    void getExchange_returnsExchange() {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final String result = realExtInstrumentsService.getExchange(TestInstrument1.FIGI);

        Assertions.assertEquals(TestInstrument1.EXCHANGE, result);
    }

    @Test
    void getExchange_throwIllegalArgumentException_whenNoInstrument() {
        final String figi = TestInstrument1.FIGI;

        final Executable executable = () -> realExtInstrumentsService.getExchange(figi);
        final String expectedMessage = "Not found instrument for FIGI '" + figi + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    @Test
    void getShare_returnsShare() {
        Mocker.mockShare(instrumentsService, TestShare2.TINKOFF_SHARE);

        final Share result = realExtInstrumentsService.getShare(TestShare2.FIGI);

        Assertions.assertEquals(TestShare2.SHARE, result);
    }

    @Test
    void getEtf_returnsEtf() {
        Mocker.mockEtf(instrumentsService, TestEtf3.TINKOFF_ETF);

        final Etf result = realExtInstrumentsService.getEtf(TestEtf3.FIGI);

        Assertions.assertEquals(TestEtf3.ETF, result);
    }

    @Test
    void getBond_returnsBond() {
        Mocker.mockBond(instrumentsService, TestBond2.TINKOFF_BOND);

        final Bond result = realExtInstrumentsService.getBond(TestBond2.FIGI);

        Assertions.assertEquals(TestBond2.BOND, result);
    }

    @Test
    void getCurrencyByFigi_returnsCurrency() {
        Mocker.mockCurrency(instrumentsService, TestCurrency2.TINKOFF_CURRENCY);

        final Currency result = realExtInstrumentsService.getCurrencyByFigi(TestCurrency2.FIGI);

        Assertions.assertEquals(TestCurrency2.CURRENCY, result);
    }

    @Test
    @DirtiesContext
    void getAllCurrencies_returnsCachedValue() {
        Mocker.mockAllCurrencies(instrumentsService, TestCurrency1.TINKOFF_CURRENCY, TestCurrency2.TINKOFF_CURRENCY);

        final List<Currency> actualResult1 = realExtInstrumentsService.getAllCurrencies();

        final List<Currency> expectedResult = List.of(TestCurrency1.CURRENCY, TestCurrency2.CURRENCY);
        Assertions.assertEquals(expectedResult, actualResult1);

        Mocker.mockAllCurrencies(instrumentsService);

        final List<Currency> actualResult2 = realExtInstrumentsService.getAllCurrencies();
        Assertions.assertEquals(expectedResult, actualResult2);
    }

    // region getCurrenciesByIsoNames tests

    @Test
    @DirtiesContext
    void getCurrenciesByIsoNames_returnsCachedValue() {
        Mocker.mockAllCurrencies(instrumentsService, TestCurrency1.TINKOFF_CURRENCY, TestCurrency2.TINKOFF_CURRENCY);

        final List<Currency> actualResult1 =
                realExtInstrumentsService.getCurrenciesByIsoNames(TestCurrency1.ISO_CURRENCY_NAME, TestCurrency2.ISO_CURRENCY_NAME);

        final List<Currency> expectedResult = List.of(TestCurrency1.CURRENCY, TestCurrency2.CURRENCY);
        Assertions.assertEquals(expectedResult, actualResult1);

        Mocker.mockAllCurrencies(instrumentsService);

        final List<Currency> actualResult2 =
                realExtInstrumentsService.getCurrenciesByIsoNames(TestCurrency1.ISO_CURRENCY_NAME, TestCurrency2.ISO_CURRENCY_NAME);
        Assertions.assertEquals(expectedResult, actualResult2);
    }

    // endregion

    // region getTradingDay tests

    @Test
    void getTradingDay_returnsTradingDay() {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final OffsetDateTime dateTime = DateTimeTestData.createDateTime(2022, 10, 3, 3);

        mockTradingSchedule(TestInstrument1.EXCHANGE, dateTime, dateTime);

        final Instant mockedNow = DateUtils.toSameDayInstant(dateTime);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final TradingDay tradingDay = realExtInstrumentsService.getTradingDay(TestInstrument1.FIGI, dateTime);

            Assertions.assertEquals(TestTradingDay1.TRADING_DAY, tradingDay);
        }
    }

    @Test
    void getTradingDay_throwsIllegalArgumentException_whenInstrumentNotFound() {
        final String figi = TestInstrument1.FIGI;

        final OffsetDateTime timestamp = DateTimeTestData.createDateTime(2022, 10, 3, 3);

        final Executable executable = () -> realExtInstrumentsService.getTradingDay(figi, timestamp);
        final String expectedMessage = "Not found instrument for FIGI '" + figi + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getTradingSchedule tests

    @Test
    void getTradingSchedule_forFuture() {
        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3);

        mockTradingSchedule(exchange, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = realExtInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

            final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    void getTradingSchedule_forFuture_adjustsFromInstant_positiveOffset() {
        final String exchange = "MOEX";

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 1, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3, offset);

        mockTradingSchedule(exchange, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = realExtInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

            final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    void getTradingSchedule_forFuture_adjustsFromInstant_negativeOffset() {
        final String exchange = "MOEX";

        final ZoneOffset offset = ZoneOffset.ofHours(-3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 22, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3, offset);

        mockTradingSchedule(exchange, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> result = realExtInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

            final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedResult, result);
        }
    }

    @Test
    void getTradingSchedule_forFuture_adjustsToInstant_positiveOffset() {
        final String exchange = "MOEX";

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 1, offset);

        mockTradingSchedule(exchange, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> result = realExtInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

            final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedResult, result);
        }
    }

    @Test
    void getTradingSchedule_forFuture_adjustsToInstant_negativeOffset() {
        final String exchange = "MOEX";

        final ZoneOffset offset = ZoneOffset.ofHours(-3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 22, offset);

        mockTradingSchedule(exchange, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> result = realExtInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

            final List<TradingDay> expectedResult = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedResult, result);
        }
    }

    @Test
    void getTradingSchedule_forPast() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.createEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.createDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = realExtInstrumentsService.getTradingSchedule(exchange, interval);

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

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    // endregion

    // region getTradingScheduleByFigi tests

    @Test
    void getTradingScheduleByFigi_forFuture_adjustsFromInstant_positiveOffset() {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 1, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3, offset);

        mockTradingSchedule(TestInstrument1.EXCHANGE, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> schedule = realExtInstrumentsService.getTradingScheduleByFigi(TestInstrument1.FIGI, Interval.of(from, to));

            final List<TradingDay> expectedSchedule = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedSchedule, schedule);
        }
    }

    @Test
    void getTradingScheduleByFigi_forFuture_adjustsFromInstant_negativeOffset() {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final ZoneOffset offset = ZoneOffset.ofHours(-3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 22, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3, offset);

        mockTradingSchedule(TestInstrument1.EXCHANGE, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> schedule = realExtInstrumentsService.getTradingScheduleByFigi(TestInstrument1.FIGI, Interval.of(from, to));

            final List<TradingDay> expectedSchedule = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedSchedule, schedule);
        }
    }

    @Test
    void getTradingScheduleByFigi_forFuture_adjustsToInstant_positiveOffset() {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final ZoneOffset offset = ZoneOffset.ofHours(3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 1, offset);

        mockTradingSchedule(TestInstrument1.EXCHANGE, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> schedule = realExtInstrumentsService.getTradingScheduleByFigi(TestInstrument1.FIGI, Interval.of(from, to));

            final List<TradingDay> expectedSchedule = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedSchedule, schedule);
        }
    }

    @Test
    void getTradingScheduleByFigi_forFuture_adjustsToInstant_negativeOffset() {
        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final ZoneOffset offset = ZoneOffset.ofHours(-3);
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3, offset);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 22, offset);

        mockTradingSchedule(TestInstrument1.EXCHANGE, from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> schedule = realExtInstrumentsService.getTradingScheduleByFigi(TestInstrument1.FIGI, Interval.of(from, to));

            final List<TradingDay> expectedSchedule = List.of(TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);

            Assertions.assertEquals(expectedSchedule, schedule);
        }
    }

    @Test
    void getTradingScheduleByFigi_forPast() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final String figi = TestInstrument1.FIGI;
        final OffsetDateTime from = DateTimeTestData.createEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.createDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, TestInstrument1.TINKOFF_INSTRUMENT);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = realExtInstrumentsService.getTradingScheduleByFigi(figi, interval);

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

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    void getTradingScheduleByFigi_throwsIllegalArgumentException_whenInstrumentNotFound() {
        final String figi = TestInstrument1.FIGI;

        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 7, 3);

        final Executable executable = () -> realExtInstrumentsService.getTradingScheduleByFigi(figi, Interval.of(from, to));
        final String expectedMessage = "Not found instrument for FIGI '" + figi + "'";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getTradingSchedules tests

    @Test
    void getTradingSchedules() {
        final OffsetDateTime from = DateTimeTestData.createDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.createDateTime(2022, 10, 8, 3);

        final String exchange1 = "MOEX";
        final String exchange2 = "SPB";

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

        final List<TradingSchedule> result = realExtInstrumentsService.getTradingSchedules(Interval.of(from, to));

        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, TestTradingDay1.TRADING_DAY, TestTradingDay2.TRADING_DAY);
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, TestTradingDay3.TRADING_DAY);

        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

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