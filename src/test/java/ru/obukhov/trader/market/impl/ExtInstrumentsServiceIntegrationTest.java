package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.bond.TestBond;
import ru.obukhov.trader.test.utils.model.bond.TestBonds;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.dividend.TestDividend;
import ru.obukhov.trader.test.utils.model.dividend.TestDividends;
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
import java.util.Collections;
import java.util.List;

@SpringBootTest
class ExtInstrumentsServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ExtInstrumentsService extInstrumentsService;

    // region getTickerByFigi tests

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsTicker_whenInstrumentFound() {
        final TestShare share = TestShares.APPLE;

        Mocker.mockTickerByFigi(instrumentsService, share.getTicker(), share.getFigi());
        final String result = extInstrumentsService.getTickerByFigi(share.getFigi());

        Assertions.assertEquals(share.getTicker(), result);
    }

    @Test
    @DirtiesContext
    void getTickerByFigi_returnsCachedValue() {
        final TestShare share = TestShares.APPLE;

        Mocker.mockTickerByFigi(instrumentsService, share.getTicker(), share.getFigi());
        extInstrumentsService.getTickerByFigi(share.getFigi());

        Mockito.when(instrumentsService.getInstrumentByFigiSync(share.getFigi())).thenReturn(null);
        final String result = extInstrumentsService.getTickerByFigi(share.getFigi());

        Assertions.assertEquals(share.getTicker(), result);
    }

    @Test
    void getTickerByFigi_throwsInstrumentNotFoundException_whenNoInstrument() {
        final String figi = TestShares.APPLE.getFigi();

        final Executable executable = () -> extInstrumentsService.getTickerByFigi(figi);
        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getExchange tests

    @Test
    @DirtiesContext
    void getExchange_returnsExchange() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        Mocker.mockInstrument(instrumentsService, instrument);

        final String result = extInstrumentsService.getExchange(instrument.getFigi());

        Assertions.assertEquals(instrument.getExchange(), result);
    }

    @Test
    @DirtiesContext
    void getExchange_returnsCachedValue() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();

        Mocker.mockInstrument(instrumentsService, instrument);
        extInstrumentsService.getExchange(figi);

        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(null);
        final String result = extInstrumentsService.getExchange(figi);

        Assertions.assertEquals(instrument.getExchange(), result);
        Mockito.verify(instrumentsService, Mockito.times(1)).getInstrumentByFigiSync(figi);
    }

    @Test
    void getExchange_throwInstrumentNotFoundException_whenNoInstrument() {
        final String figi = TestInstruments.APPLE.getFigi();

        final Executable executable = () -> extInstrumentsService.getExchange(figi);
        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getExchanges tests

    @Test
    @DirtiesContext
    void getExchanges_returnsExchanges() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);
        Mocker.mockInstrument(instrumentsService, instrument2);

        final List<String> figies = List.of(instrument1.getFigi(), instrument2.getFigi());
        final List<String> exchanges = extInstrumentsService.getExchanges(figies);

        final List<String> expectedExchanges = List.of(instrument1.getExchange(), instrument2.getExchange());
        Assertions.assertEquals(expectedExchanges, exchanges);
    }

    @Test
    @DirtiesContext
    void getExchanges_returnsCachedValues() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);
        Mocker.mockInstrument(instrumentsService, instrument2);

        final List<String> figies = List.of(instrument1.getFigi(), instrument2.getFigi());
        extInstrumentsService.getExchanges(figies);

        Mockito.when(instrumentsService.getInstrumentByFigiSync(instrument1.getFigi())).thenReturn(null);
        Mockito.when(instrumentsService.getInstrumentByFigiSync(instrument2.getFigi())).thenReturn(null);
        final List<String> exchanges = extInstrumentsService.getExchanges(figies);

        final List<String> expectedExchanges = List.of(instrument1.getExchange(), instrument2.getExchange());
        Assertions.assertEquals(expectedExchanges, exchanges);
        Mockito.verify(instrumentsService, Mockito.times(1)).getInstrumentByFigiSync(instrument1.getFigi());
        Mockito.verify(instrumentsService, Mockito.times(1)).getInstrumentByFigiSync(instrument2.getFigi());
    }

    @Test
    void getExchanges_throwInstrumentNotFoundException_whenNoInstrument() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);

        final List<String> figies = List.of(instrument1.getFigi(), instrument2.getFigi());
        final Executable executable = () -> extInstrumentsService.getExchanges(figies);

        final String expectedMessage = "Instrument not found for id " + instrument2.getFigi();
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

    @Test
    void getInstrument() {
        final TestInstrument testInstrument = TestInstruments.SBER;
        final String figi = testInstrument.getFigi();
        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        final Instrument actualResult1 = extInstrumentsService.getInstrument(figi);

        Assertions.assertEquals(testInstrument.instrument(), actualResult1);

        Mockito.when(instrumentsService.getInstrumentByFigiSync(figi)).thenReturn(null);

        final Instrument actualResult2 = extInstrumentsService.getInstrument(figi);
        Assertions.assertEquals(testInstrument.instrument(), actualResult2);
    }

    // region getShare tests

    @Test
    @DirtiesContext
    void getShare_returnsShare() {
        final TestShare testShare = TestShares.SBER;

        Mocker.mockShare(instrumentsService, testShare);

        final Share result = extInstrumentsService.getShare(testShare.getFigi());

        Assertions.assertEquals(testShare.share(), result);
    }

    @Test
    @DirtiesContext
    void getShare_returnsCachedShare() {
        final TestShare testShare = TestShares.SBER;
        final String figi = testShare.getFigi();

        Mocker.mockShare(instrumentsService, testShare);
        extInstrumentsService.getShare(figi);

        Mockito.when(instrumentsService.getShareByFigiSync(figi)).thenReturn(null);
        final Share result = extInstrumentsService.getShare(figi);

        Assertions.assertEquals(testShare.share(), result);
    }

    // endregion

    @Test
    @DirtiesContext
    void getShares_returnsCachedValue() {
        final TestShare share1 = TestShares.APPLE;
        final TestShare share2 = TestShares.SBER;
        final TestShare share3 = TestShares.YANDEX;
        final TestShare share4 = TestShares.DIOD;

        final List<String> figies = List.of(share1.getFigi(), share2.getFigi(), share4.getFigi());
        Mocker.mockAllShares(instrumentsService, share1, share2, share3, share4);
        final List<Share> actualResult1 = extInstrumentsService.getShares(figies);

        Mocker.mockAllShares(instrumentsService, share1, share3);
        final List<Share> actualResult2 = extInstrumentsService.getShares(figies);

        final List<Share> expectedResult = List.of(share1.share(), share2.share(), share4.share());
        AssertUtils.assertEquals(expectedResult, actualResult1);
        AssertUtils.assertEquals(expectedResult, actualResult2);
    }

    @Test
    @DirtiesContext
    void getAllShares_returnsCachedValue() {
        final TestShare testShare1 = TestShares.APPLE;
        final TestShare testShare2 = TestShares.SBER;
        final TestShare testShare3 = TestShares.YANDEX;
        final TestShare testShare4 = TestShares.DIOD;

        final List<TestShare> testShares = List.of(testShare1, testShare2, testShare3, testShare4);
        final List<Share> shares = testShares.stream().map(TestShare::share).toList();

        Mocker.mockAllShares(instrumentsService, testShares);
        final List<Share> actualResult1 = extInstrumentsService.getAllShares();
        Mocker.mockAllShares(instrumentsService, testShare1, testShare3);
        final List<Share> actualResult2 = extInstrumentsService.getAllShares();

        AssertUtils.assertEquals(shares, actualResult1);
        AssertUtils.assertEquals(shares, actualResult2);
    }

    @Test
    void getEtf_returnsEtf() {
        final TestEtf testEtf = TestEtfs.EZA;

        Mocker.mockEtf(instrumentsService, testEtf.tinkoffEtf());

        final Etf result = extInstrumentsService.getEtf(testEtf.getFigi());

        Assertions.assertEquals(testEtf.etf(), result);
    }

    @Test
    void getBond_returnsBond() {
        final TestBond testBond = TestBonds.KAZAKHSTAN;
        Mocker.mockBond(instrumentsService, testBond.tinkoffBond());

        final Bond result = extInstrumentsService.getBond(testBond.getFigi());

        Assertions.assertEquals(testBond.bond(), result);
    }

    @Test
    void getCurrencyByFigi_returnsCurrency() {
        final TestCurrency currency = TestCurrencies.RUB;
        Mocker.mockCurrency(instrumentsService, currency);

        final Currency result = extInstrumentsService.getCurrencyByFigi(currency.getFigi());

        Assertions.assertEquals(TestCurrencies.RUB.currency(), result);
    }

    @Test
    @DirtiesContext
    void getAllCurrencies_returnsCachedValue() {
        final TestCurrency testCurrency1 = TestCurrencies.USD;
        final TestCurrency testCurrency2 = TestCurrencies.RUB;

        Mocker.mockAllCurrencies(instrumentsService, testCurrency1, testCurrency2);

        final List<Currency> actualResult1 = extInstrumentsService.getAllCurrencies();

        final List<Currency> expectedResult = List.of(testCurrency1.currency(), testCurrency2.currency());
        Assertions.assertEquals(expectedResult, actualResult1);

        Mocker.mockAllCurrencies(instrumentsService);

        final List<Currency> actualResult2 = extInstrumentsService.getAllCurrencies();
        Assertions.assertEquals(expectedResult, actualResult2);
    }

    // region getCurrenciesByIsoNames tests

    @Test
    @DirtiesContext
    void getCurrenciesByIsoNames() {
        final TestCurrency currency1 = TestCurrencies.USD;
        final TestCurrency currency2 = TestCurrencies.RUB;

        Mocker.mockAllCurrencies(instrumentsService, currency1, currency2);

        final List<Currency> actualResult1 = extInstrumentsService.getCurrenciesByIsoNames(
                currency1.getIsoCurrencyName(),
                currency2.getIsoCurrencyName(),
                currency1.getIsoCurrencyName(),
                currency2.getIsoCurrencyName(),
                currency2.getIsoCurrencyName()
        );

        final List<Currency> expectedResult = List.of(currency1.currency(), currency2.currency());
        Assertions.assertEquals(expectedResult, actualResult1);

        Mocker.mockAllCurrencies(instrumentsService);

        final List<Currency> actualResult2 = extInstrumentsService.getCurrenciesByIsoNames(
                currency1.getIsoCurrencyName(),
                currency2.getIsoCurrencyName(),
                currency1.getIsoCurrencyName(),
                currency2.getIsoCurrencyName(),
                currency2.getIsoCurrencyName()
        );
        Assertions.assertEquals(expectedResult, actualResult2);
    }

    // endregion

    // region getTradingDay tests

    @Test
    @DirtiesContext
    void getTradingDay_returnsTradingDay() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        Mocker.mockInstrument(instrumentsService, instrument);

        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2022, 10, 3, 3);

        mockTradingSchedule(instrument.getExchange(), dateTime, dateTime);

        final OffsetDateTime mockedNow = DateUtils.toStartOfDay(dateTime);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final TradingDay tradingDay = extInstrumentsService.getTradingDay(instrument.getFigi(), dateTime);

            Assertions.assertEquals(TestTradingDays.TRADING_DAY1.tradingDay(), tradingDay);
        }
    }

    @Test
    void getTradingDay_throwsIllegalArgumentException_whenInstrumentNotFound() {
        final String figi = TestInstruments.APPLE.getFigi();

        final OffsetDateTime timestamp = DateTimeTestData.newDateTime(2022, 10, 3, 3);

        final Executable executable = () -> extInstrumentsService.getTradingDay(figi, timestamp);
        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getTradingSchedule tests

    @Test
    void getTradingSchedule_forFuture() {
        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 7, 3);

        mockTradingSchedule(exchange, from, to);

        final OffsetDateTime mockedNow = DateUtils.toStartOfDay(from);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> dateTimeStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingSchedule(exchange, Interval.of(from, to));

            final List<TradingDay> expectedResult = List.of(TestTradingDays.TRADING_DAY1.tradingDay(), TestTradingDays.TRADING_DAY2.tradingDay());

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    void getTradingSchedule_forPast() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final String exchange = "MOEX";
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingSchedule(exchange, interval);

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

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    // endregion

    // region getTradingScheduleByFigi tests

    @Test
    @DirtiesContext
    void getTradingScheduleByFigi_forFuture() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(2023, 8, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 8, 27);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, instrument);
        mockTradingSchedule(instrument.getExchange(), from, to);

        final OffsetDateTime mockedNow = from.minusNanos(1);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> mockedStatic = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigi(figi, interval);

            final List<TradingDay> expectedResult = List.of(
                    TestTradingDays.TRADING_DAY1.tradingDay(),
                    TestTradingDays.TRADING_DAY2.tradingDay()
            );

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigi_forPast() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, instrument);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigi(figi, interval);

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

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigi_usesCachedExchange() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 20);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, instrument);
        extInstrumentsService.getExchange(figi); // to cache exchange
        Mockito.when(instrumentsService.getShareByFigiSync(figi)).thenReturn(null);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigi(figi, interval);

            final List<TradingDay> expectedResult = List.of(
                    TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                    TestData.newTradingDay(false, year, month, 19, hour, durationHours)
            );

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigi_throwsIllegalArgumentException_whenInstrumentNotFound() {
        final String figi = TestInstruments.APPLE.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 7, 3);

        final Executable executable = () -> extInstrumentsService.getTradingScheduleByFigi(figi, Interval.of(from, to));
        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getTradingScheduleByFigies tests

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_singleFigi_forFuture() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(2023, 8, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 8, 27);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, instrument);
        mockTradingSchedule(instrument.getExchange(), from, to);

        final OffsetDateTime mockedNow = from.minusNanos(1);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> mockedStatic = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigies(List.of(figi), interval);

            final List<TradingDay> expectedResult = List.of(
                    TestTradingDays.TRADING_DAY1.tradingDay(),
                    TestTradingDays.TRADING_DAY2.tradingDay()
            );

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_singleFigi_forPast() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, instrument);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigies(List.of(figi), interval);

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

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_singleFigi_usesCachedExchange() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final ru.tinkoff.piapi.contract.v1.Instrument instrument = TestInstruments.APPLE.tinkoffInstrument();
        final String figi = instrument.getFigi();
        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 20);
        final Interval interval = Interval.of(from, to);

        Mocker.mockInstrument(instrumentsService, instrument);
        extInstrumentsService.getExchange(figi); // to cache exchange
        Mockito.when(instrumentsService.getShareByFigiSync(figi)).thenReturn(null);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigies(List.of(figi), interval);

            final List<TradingDay> expectedResult = List.of(
                    TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                    TestData.newTradingDay(false, year, month, 19, hour, durationHours)
            );

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_singleFigi_throwsIllegalArgumentException_whenInstrumentNotFound() {
        final String figi = TestInstruments.APPLE.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 7, 3);

        final Executable executable = () -> extInstrumentsService.getTradingScheduleByFigies(List.of(figi), Interval.of(from, to));
        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    @Test
    void getTradingScheduleByFigies_throwsIllegalArgumentException_whenNoFigies() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 7, 3);

        final Executable executable = () -> extInstrumentsService.getTradingScheduleByFigies(Collections.emptyList(), Interval.of(from, to));
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "figies must not be empty");
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_multipleFigies_forFuture() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);
        Mocker.mockInstrument(instrumentsService, instrument2);

        final String exchange1 = instrument1.getExchange();
        final String exchange2 = instrument2.getExchange();

        final String figi1 = instrument1.getFigi();
        final String figi2 = instrument2.getFigi();
        final List<String> figies = List.of(figi1, figi2);

        final OffsetDateTime from = DateTimeTestData.newEndOfDay(2023, 8, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 8, 27);
        final Interval interval = Interval.of(from, to);

        final Instant fromInstant = from.toInstant();
        final Instant toInstant = to.toInstant();

        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule1 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange1)
                .addDays(TestData.newTinkoffTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 18, 7), DateTimeTestData.newDateTime(2023, 8, 18, 19)))
                .addDays(TestData.newTinkoffTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 19, 7), DateTimeTestData.newDateTime(2023, 8, 19, 19)))
                .addDays(TestData.newTinkoffTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 20, 7), DateTimeTestData.newDateTime(2023, 8, 20, 19)))
                .addDays(TestData.newTinkoffTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 21, 7), DateTimeTestData.newDateTime(2023, 8, 21, 19)))
                .build();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule2 = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange2)
                .addDays(TestData.newTinkoffTradingDay(false, DateTimeTestData.newDateTime(2023, 8, 18, 10), DateTimeTestData.newDateTime(2023, 8, 19, 2)))
                .addDays(TestData.newTinkoffTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 19, 7), DateTimeTestData.newDateTime(2023, 8, 19, 19)))
                .addDays(TestData.newTinkoffTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 21, 10), DateTimeTestData.newDateTime(2023, 8, 22, 2)))
                .build();

        Mockito.when(instrumentsService.getTradingScheduleSync(exchange1, fromInstant, toInstant)).thenReturn(tradingSchedule1);
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange2, fromInstant, toInstant)).thenReturn(tradingSchedule2);

        final OffsetDateTime mockedNow = from.minusNanos(1);
        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> mockedStatic = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigies(figies, interval);

            final List<TradingDay> expectedResult = List.of(
                    TestData.newTradingDay(false, DateTimeTestData.newDateTime(2023, 8, 18, 10), DateTimeTestData.newDateTime(2023, 8, 18, 19)),
                    TestData.newTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 19, 7), DateTimeTestData.newDateTime(2023, 8, 19, 19)),
                    TestData.newTradingDay(true, DateTimeTestData.newDateTime(2023, 8, 21, 10), DateTimeTestData.newDateTime(2023, 8, 21, 19))
            );

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_multipleFigies_forPast() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);
        Mocker.mockInstrument(instrumentsService, instrument2);

        final String figi1 = instrument1.getFigi();
        final String figi2 = instrument2.getFigi();
        final List<String> figies = List.of(figi1, figi2);

        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 27);
        final Interval interval = Interval.of(from, to);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigies(figies, interval);

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

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_multipleFigies_usesCachedExchange() {
        final int year = 2023;
        final int month = 8;
        final int hour = 12;
        final int durationHours = 8;

        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);
        Mocker.mockInstrument(instrumentsService, instrument2);

        final String figi1 = instrument1.getFigi();
        final String figi2 = instrument2.getFigi();

        final OffsetDateTime from = DateTimeTestData.newEndOfDay(year, month, 18);
        final OffsetDateTime to = DateTimeTestData.newDateTime(year, month, 20);
        final Interval interval = Interval.of(from, to);

        extInstrumentsService.getExchange(figi1); // to cache exchange
        extInstrumentsService.getExchange(figi2); // to cache exchange

        Mockito.when(instrumentsService.getShareByFigiSync(figi1)).thenReturn(null);
        Mockito.when(instrumentsService.getShareByFigiSync(figi2)).thenReturn(null);

        final Instant mockedNow = DateUtils.toSameDayInstant(from).plusMillis(1);
        try (@SuppressWarnings("unused") final MockedStatic<Instant> instantStaticMock = Mocker.mockNow(mockedNow)) {
            final List<TradingDay> actualResult = extInstrumentsService.getTradingScheduleByFigies(List.of(figi1, figi2), interval);

            final List<TradingDay> expectedResult = List.of(
                    TestData.newTradingDay(true, year, month, 18, hour, durationHours),
                    TestData.newTradingDay(false, year, month, 19, hour, durationHours)
            );

            Assertions.assertEquals(expectedResult, actualResult);
        }
    }

    @Test
    @DirtiesContext
    void getTradingScheduleByFigies_multipleFigies_throwsIllegalArgumentException_whenInstrumentNotFound() {
        final ru.tinkoff.piapi.contract.v1.Instrument instrument1 = TestInstruments.APPLE.tinkoffInstrument();
        final ru.tinkoff.piapi.contract.v1.Instrument instrument2 = TestInstruments.SBER.tinkoffInstrument();

        Mocker.mockInstrument(instrumentsService, instrument1);

        final String figi1 = instrument1.getFigi();
        final String figi2 = instrument2.getFigi();

        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 7, 3);
        final Interval interval = Interval.of(from, to);

        final Executable executable = () -> extInstrumentsService.getTradingScheduleByFigies(List.of(figi1, figi2), interval);
        final String expectedMessage = "Instrument not found for id " + figi2;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getTradingSchedules tests

    @Test
    void getTradingSchedules() {
        final OffsetDateTime from = DateTimeTestData.newDateTime(2022, 10, 3, 3);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2022, 10, 8, 3);

        final String exchange1 = "MOEX";
        final String exchange2 = "SPB";

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

        final List<TradingSchedule> result = extInstrumentsService.getTradingSchedules(Interval.of(from, to));

        final TradingSchedule expectedTradingSchedule1 = new TradingSchedule(exchange1, testTradingDay1.tradingDay(), testTradingDay2.tradingDay());
        final TradingSchedule expectedTradingSchedule2 = new TradingSchedule(exchange2, testTradingDay3.tradingDay());

        final List<TradingSchedule> expectedResult = List.of(expectedTradingSchedule1, expectedTradingSchedule2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    private void mockTradingSchedule(final String exchange, final OffsetDateTime from, final OffsetDateTime to) {
        final Instant fromInstant = from.toInstant();
        final Instant toInstant = to.toInstant();
        final ru.tinkoff.piapi.contract.v1.TradingSchedule tradingSchedule = ru.tinkoff.piapi.contract.v1.TradingSchedule.newBuilder()
                .setExchange(exchange)
                .addDays(TestTradingDays.TRADING_DAY1.tinkoffTradingDay())
                .addDays(TestTradingDays.TRADING_DAY2.tinkoffTradingDay())
                .build();
        Mockito.when(instrumentsService.getTradingScheduleSync(exchange, fromInstant, toInstant)).thenReturn(tradingSchedule);
    }

    @Test
    void getDividends() {
        final String figi = TestShares.SBER.getFigi();
        final OffsetDateTime from = DateTimeTestData.newDateTime(2010, 1, 1);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2023, 1, 1);

        final TestDividend testDividend1 = TestDividends.TEST_DIVIDEND1;
        final TestDividend testDividend2 = TestDividends.TEST_DIVIDEND2;
        final List<ru.tinkoff.piapi.contract.v1.Dividend> tinkoffDividends =
                List.of(testDividend1.tinkoffDividend(), testDividend2.tinkoffDividend());
        Mockito.when(instrumentsService.getDividendsSync(figi, from.toInstant(), to.toInstant())).thenReturn(tinkoffDividends);

        final List<Dividend> actualResult = extInstrumentsService.getDividends(figi, Interval.of(from, to));

        final List<Dividend> expectedResult = List.of(testDividend1.dividend());
        Assertions.assertEquals(expectedResult, actualResult);
    }

}