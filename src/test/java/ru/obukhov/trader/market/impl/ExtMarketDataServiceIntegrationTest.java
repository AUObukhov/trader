package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.obukhov.trader.IntegrationTest;
import ru.obukhov.trader.common.exception.InstrumentNotFoundException;
import ru.obukhov.trader.common.exception.MultipleInstrumentsFoundException;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.CandleMocker;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.CandleBuilder;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.HistoricCandleBuilder;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.currency.TestCurrencies;
import ru.obukhov.trader.test.utils.model.currency.TestCurrency;
import ru.obukhov.trader.test.utils.model.instrument.TestInstrument;
import ru.obukhov.trader.test.utils.model.instrument.TestInstruments;
import ru.obukhov.trader.test.utils.model.share.TestShares;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.LastPrice;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
class ExtMarketDataServiceIntegrationTest extends IntegrationTest {

    @Autowired
    private ExtMarketDataService extMarketDataService;

    // region getCandles tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCandles_filtersCandles() {
        return Stream.of(
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_1_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 8, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 59), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 9, 59), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 6, 23, 59), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 59), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 9, 59), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 8, 16), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 9) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_2_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 8, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 58), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 9, 58), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 6, 23, 58), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 58), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 9, 58), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 8, 16), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 9) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_3_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 8, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 57), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 9, 57), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 6, 23, 57), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 57), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 9, 57), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 8, 16), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 9) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_5_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 8, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 55), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 9, 55), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 6, 23, 55), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 55), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 9, 55), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 8, 16), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 9) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_10_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 8, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 50), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 9, 50), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 6, 23, 50), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 50), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 9, 50), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 8, 16), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 9) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_15_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 8, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 8, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 45), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 9, 45), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 6, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 6, 23, 45), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 45), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 9, 45), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 8, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 8, 16), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 9) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_30_MIN,
                        DateTimeTestData.newDateTime(2020, 1, 7, 10), // from1
                        DateTimeTestData.newDateTime(2020, 1, 12, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 6), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 1, 13, 0, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 1, 13, 15), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 5, 23, 30), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 7, 9, 30), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 7, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 7, 23, 30), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 8), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 9, 23, 30), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 12), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 12, 9, 30), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 1, 12, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 13), // candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 14) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_HOUR,
                        // weeks: 6-12, 13-19, 20-26, 27-2
                        DateTimeTestData.newDateTime(2020, 1, 15, 10), // from1
                        DateTimeTestData.newDateTime(2020, 2, 2, 10), // to1
                        DateTimeTestData.newDateTime(2020, 1, 13), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 2, 2, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 2, 2, 20), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 12, 23), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 15, 9), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 1, 15, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 1, 19, 23), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 1, 20), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 26, 23), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 1, 27), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 2, 2, 9), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 2, 2, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 2, 2, 16), // some candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 2, 3) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_2_HOUR,
                        DateTimeTestData.newDateTime(2020, 2, 5, 10), // from1
                        DateTimeTestData.newDateTime(2020, 4, 22, 10), // to1
                        DateTimeTestData.newDateTime(2020, 2, 1), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 4, 25, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 4, 28), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 31, 22), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 2, 5, 8), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 2, 5, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 2, 29, 22), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 3, 1), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 3, 31, 22), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 4, 1), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 4, 22, 8), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 4, 22, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 4, 25, 16), // some candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 5, 1) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_4_HOUR,
                        DateTimeTestData.newDateTime(2020, 2, 5, 10), // from1
                        DateTimeTestData.newDateTime(2020, 4, 22, 10), // to1
                        DateTimeTestData.newDateTime(2020, 2, 1), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2020, 4, 25, 16, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2020, 4, 28), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2020, 1, 31, 20), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2020, 2, 5, 8), // last candle before from1
                                DateTimeTestData.newDateTime(2020, 2, 5, 10), // from1 exactly
                                DateTimeTestData.newDateTime(2020, 2, 29, 20), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2020, 3, 1), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 3, 31, 20), // last candle of middle interval
                                DateTimeTestData.newDateTime(2020, 4, 1), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2020, 4, 22, 8), // last candle before to1
                                DateTimeTestData.newDateTime(2020, 4, 22, 10), // to1 exactly
                                DateTimeTestData.newDateTime(2020, 4, 25, 16), // some candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 5, 1) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_DAY,
                        DateTimeTestData.newDateTime(2017, 3, 1), // from1
                        DateTimeTestData.newDateTime(2019, 7, 1), // to1
                        DateTimeTestData.newDateTime(2017, 1, 1), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2019, 10, 1, 0, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2019, 11, 28), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2016, 12, 31), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2017, 2, 28), // last candle before from1
                                DateTimeTestData.newDateTime(2017, 3, 1), // from1 exactly
                                DateTimeTestData.newDateTime(2017, 12, 1), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2018, 1, 1), // first candle of middle interval
                                DateTimeTestData.newDateTime(2018, 12, 1), // last candle of middle interval
                                DateTimeTestData.newDateTime(2019, 1, 1), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2019, 6, 1), // last candle before to1
                                DateTimeTestData.newDateTime(2019, 7, 1), // to1 exactly
                                DateTimeTestData.newDateTime(2019, 9, 1), // some candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2020, 1, 1) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_WEEK,
                        // couple of years: 2017-2018, 2019-2020, 2021-2022, 2023-2024
                        DateTimeTestData.newDateTime(2017, 8, 1), // from1
                        DateTimeTestData.newDateTime(2021, 10, 1), // to1
                        DateTimeTestData.newDateTime(2017, 1, 1), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2022, 2, 14, 10, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2022, 6, 10), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(2016, 12, 26), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(2017, 8, 31), // last candle before from1
                                DateTimeTestData.newDateTime(2017, 8, 1), // from1 exactly
                                DateTimeTestData.newDateTime(2018, 12, 31), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2019, 1, 7), // first candle of middle interval
                                DateTimeTestData.newDateTime(2020, 12, 28), // last candle of middle interval
                                DateTimeTestData.newDateTime(2021, 1, 4), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2021, 9, 27), // last candle before to1
                                DateTimeTestData.newDateTime(2021, 10, 1), // to1 exactly
                                DateTimeTestData.newDateTime(2022, 2, 14), // some candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2023, 1, 2) // first candle from interval after interval with to1
                        }
                ),
                Arguments.of(
                        CandleInterval.CANDLE_INTERVAL_MONTH,
                        DateTimeTestData.newDateTime(1994, 1, 1), // from1
                        DateTimeTestData.newDateTime(2018, 1, 1), // to1
                        DateTimeTestData.newDateTime(1991, 1, 1), // from2 - start of interval with from1
                        DateTimeTestData.newDateTime(2018, 6, 1, 10, 0, 0, 1), // to2 - first nanosecond after last candle in interval with to1
                        DateTimeTestData.newDateTime(2019, 3, 10), // now - any time in interval with to1 after to2
                        new OffsetDateTime[]{
                                DateTimeTestData.newDateTime(1990, 12, 1), // last candle of interval before interval with from1
                                DateTimeTestData.newDateTime(1993, 12, 1), // last candle before from1
                                DateTimeTestData.newDateTime(1994, 1, 1), // from1 exactly
                                DateTimeTestData.newDateTime(2000, 12, 1), // last candle of interval with from1
                                DateTimeTestData.newDateTime(2001, 1, 1), // first candle of middle interval
                                DateTimeTestData.newDateTime(2010, 12, 1), // last candle of middle interval
                                DateTimeTestData.newDateTime(2011, 1, 1), // first candle of interval with to1
                                DateTimeTestData.newDateTime(2017, 12, 1), // last candle before to1
                                DateTimeTestData.newDateTime(2018, 1, 1), // to1 exactly
                                DateTimeTestData.newDateTime(2018, 6, 1), // some candle after to1 from interval with to1
                                DateTimeTestData.newDateTime(2023, 1, 1) // first candle from interval after interval with to1
                        }
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCandles_filtersCandles")
    @DirtiesContext
    void getCandles_filtersCandles(
            final CandleInterval candleInterval,
            final OffsetDateTime from1, final OffsetDateTime to1,
            final OffsetDateTime from2, final OffsetDateTime to2,
            final OffsetDateTime now,
            final OffsetDateTime[] candlesTimes
    ) {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        final CandleMocker candleMocker = new CandleMocker(marketDataService, figi, candleInterval);
        for (final OffsetDateTime candleTime : candlesTimes) {
            candleMocker.add(candleTime);
        }
        candleMocker.mock();

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(now)) {
            final List<Candle> candles = extMarketDataService.getCandles(figi, Interval.of(from1, to1), candleInterval);

            Assertions.assertEquals(6, candles.size());
            Assertions.assertEquals(candlesTimes[2], candles.get(0).getTime());
            Assertions.assertEquals(candlesTimes[3], candles.get(1).getTime());
            Assertions.assertEquals(candlesTimes[4], candles.get(2).getTime());
            Assertions.assertEquals(candlesTimes[5], candles.get(3).getTime());
            Assertions.assertEquals(candlesTimes[6], candles.get(4).getTime());
            Assertions.assertEquals(candlesTimes[7], candles.get(5).getTime());

            // caching test
            Mocker.mockEmptyCandles(marketDataService, figi, candleInterval);
            final List<Candle> cachedCandles = extMarketDataService.getCandles(figi, Interval.of(from2, to2), candleInterval);

            Assertions.assertEquals(5, cachedCandles.size());
            Assertions.assertEquals(candlesTimes[1], cachedCandles.get(0).getTime());
            Assertions.assertEquals(candlesTimes[2], cachedCandles.get(1).getTime());
            Assertions.assertEquals(candlesTimes[3], cachedCandles.get(2).getTime());
            Assertions.assertEquals(candlesTimes[4], cachedCandles.get(3).getTime());
            Assertions.assertEquals(candlesTimes[5], cachedCandles.get(4).getTime());
        }
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirst1MinCandle_whenFromIsNull() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        final OffsetDateTime first1MinCandleDate = testInstrument.instrument().first1MinCandleDate();
        final OffsetDateTime to = first1MinCandleDate.plusMinutes(1);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, first1MinCandleDate.minusNanos(1))
                .add(2, first1MinCandleDate)
                .add(3, to.minusNanos(1))
                .add(4, to)
                .mock();

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCandles_adjustsFrom_whenFromIsNull() {
        return Stream.of(
                Arguments.of(CandleInterval.CANDLE_INTERVAL_2_MIN, 1),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_3_MIN, 2),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_5_MIN, 4),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_10_MIN, 9),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_30_MIN, 29),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_HOUR, 59),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_2_HOUR, 299),
                Arguments.of(CandleInterval.CANDLE_INTERVAL_4_HOUR, 299)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCandles_adjustsFrom_whenFromIsNull")
    @DirtiesContext
    void getCandles_adjustsFrom_whenFromIsNull(final CandleInterval candleInterval, final int adjustmentInMinutes) {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        final OffsetDateTime first1MinCandleDate = testInstrument.instrument().first1MinCandleDate();
        final OffsetDateTime to = first1MinCandleDate.plusMinutes(adjustmentInMinutes);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, first1MinCandleDate.minusMinutes(adjustmentInMinutes).minusNanos(1))
                .add(2, first1MinCandleDate.minusMinutes(adjustmentInMinutes))
                .add(3, first1MinCandleDate)
                .add(4, to.minusNanos(1))
                .add(5, to)
                .mock();

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
        AssertUtils.assertEquals(4, candles.get(2).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirst1DayCandle_whenFromIsNull() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        final OffsetDateTime first1DayCandleDate = testInstrument.instrument().first1DayCandleDate();
        final OffsetDateTime to = first1DayCandleDate.plusDays(1);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, first1DayCandleDate.minusHours(3).minusNanos(1))
                .add(2, first1DayCandleDate.minusHours(3))
                .add(3, first1DayCandleDate)
                .add(4, to.minusNanos(1))
                .add(5, to)
                .mock();

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(3, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
        AssertUtils.assertEquals(4, candles.get(2).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstWeekCandle_whenFromIsNull_andFirst1DayCandleDateIsFirstNanoOfWeek() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2020, 7, 13, 10);
        final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument = TestData.newTinkoffInstrument(figi, first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_WEEK;

        Mocker.mockInstrument(instrumentsService, tinkoffInstrument);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.newDateTime(2020, 7, 6, 10))
                .add(2, DateTimeTestData.newDateTime(2020, 7, 13, 10))
                .add(3, DateTimeTestData.newDateTime(2020, 7, 20, 10))
                .add(4, DateTimeTestData.newDateTime(2020, 7, 27, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 7, 27, 10);

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstWeekCandle_whenFromIsNull_andFirst1DayCandleDateIsMiddleOfWeek() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2020, 7, 17, 10);
        final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument = TestData.newTinkoffInstrument(figi, first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_WEEK;

        Mocker.mockInstrument(instrumentsService, tinkoffInstrument);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.newDateTime(2020, 7, 6, 10))
                .add(2, DateTimeTestData.newDateTime(2020, 7, 13, 10))
                .add(3, DateTimeTestData.newDateTime(2020, 7, 20, 10))
                .add(4, DateTimeTestData.newDateTime(2020, 7, 27, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 7, 27, 10);

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstWeekCandle_whenFromIsNull_andFirst1DayCandleDateIsLastNanoOfWeek() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2020, 7, 20, 2, 59, 59, 999_999_999);
        final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument = TestData.newTinkoffInstrument(figi, first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_WEEK;

        Mocker.mockInstrument(instrumentsService, tinkoffInstrument);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.newDateTime(2020, 7, 6, 10))
                .add(2, DateTimeTestData.newDateTime(2020, 7, 13, 10))
                .add(3, DateTimeTestData.newDateTime(2020, 7, 20, 10))
                .add(4, DateTimeTestData.newDateTime(2020, 7, 27, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 7, 27, 10);

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstMonthCandle_whenFromIsNull_andFirst1DayCandleDateIsFirstNanoOfMonth() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2010, 2, 1, 3);
        final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument = TestData.newTinkoffInstrument(figi, first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_MONTH;

        Mocker.mockInstrument(instrumentsService, tinkoffInstrument);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.newDateTime(2010, 1, 1, 10))
                .add(2, DateTimeTestData.newDateTime(2010, 2, 1, 10))
                .add(3, DateTimeTestData.newDateTime(2010, 3, 1, 10))
                .add(4, DateTimeTestData.newDateTime(2010, 4, 1, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2010, 4, 1, 10);

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstMonthCandle_whenFromIsNull_andFirst1DayCandleDateIsMiddleOfMonth() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2010, 2, 10, 10);
        final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument = TestData.newTinkoffInstrument(figi, first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_MONTH;

        Mocker.mockInstrument(instrumentsService, tinkoffInstrument);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.newDateTime(2010, 1, 1, 10))
                .add(2, DateTimeTestData.newDateTime(2010, 2, 1, 10))
                .add(3, DateTimeTestData.newDateTime(2010, 3, 1, 10))
                .add(4, DateTimeTestData.newDateTime(2010, 4, 1, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2010, 4, 1, 10);

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstMonthCandle_whenFromIsNull_andFirst1DayCandleDateIsLastNanoOfMonth() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2010, 3, 1, 2, 59, 59, 999_999_999);
        final ru.tinkoff.piapi.contract.v1.Instrument tinkoffInstrument = TestData.newTinkoffInstrument(figi, first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_MONTH;

        Mocker.mockInstrument(instrumentsService, tinkoffInstrument);

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, DateTimeTestData.newDateTime(2010, 1, 1, 10))
                .add(2, DateTimeTestData.newDateTime(2010, 2, 1, 10))
                .add(3, DateTimeTestData.newDateTime(2010, 3, 1, 10))
                .add(4, DateTimeTestData.newDateTime(2010, 4, 1, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2010, 4, 1, 10);

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.get(0).getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    private List<Candle> getCandlesAndTestCaching(final String figi, final Interval interval, final CandleInterval candleInterval) {
        List<Candle> candles = extMarketDataService.getCandles(figi, interval, candleInterval);

        Mocker.mockEmptyCandles(marketDataService, figi, candleInterval);
        List<Candle> cachedCandles = extMarketDataService.getCandles(figi, interval, candleInterval);

        Assertions.assertEquals(candles, cachedCandles);
        return candles;
    }

    // endregion

    // region getLastPrice test

    @Test
    @DirtiesContext
    void getLastPrice_returnsPrice_whenToAfterFirst1MinCandle() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();
        final OffsetDateTime to = DateTimeTestData.newEndOfDay(2020, 1, 10);
        final OffsetDateTime candlesTo = testInstrument.instrument().first1MinCandleDate().plusDays(1);
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(candlesTo);
        final int close = 10;

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getLastPrice(figi, to);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getLastPrice_returnsPrice_whenToAfterFirst1DayCandle() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();
        final OffsetDateTime to = DateTimeTestData.newDateTime(1988, 9, 15);
        final OffsetDateTime candlesTo = testInstrument.instrument().first1DayCandleDate().plusDays(1);
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(candlesTo);
        final int close = 10;

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getLastPrice(figi, to);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getLastPrice_throwsIllegalArgumentException_whenToBeforeAllCandles() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();
        final OffsetDateTime to = testInstrument.instrument().first1DayCandleDate().minusDays(1);
        final OffsetDateTime candlesTo = testInstrument.instrument().first1DayCandleDate().plusDays(1);
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(candlesTo);
        final int close = 10;

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final String expectedMessage = "No candles found for FIGI " + figi + " before " + to;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extMarketDataService.getLastPrice(figi, to), expectedMessage);
    }

    @Test
    @DirtiesContext
    void getLastPrice_throwsIllegalArgumentException_whenNoCandlesFound() {
        final TestInstrument testInstrument = TestInstruments.APPLE;
        final String figi = testInstrument.instrument().figi();
        final OffsetDateTime to = DateTimeTestData.newEndOfDay(2020, 1, 10);
        final int close = 10;

        Mocker.mockInstrument(instrumentsService, testInstrument.tinkoffInstrument());

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, DateTimeTestData.newDateTime(2015, 2, 1))
                .mock();

        final String expectedMessage = "No candles found for FIGI " + figi + " before " + to;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extMarketDataService.getLastPrice(figi, to), expectedMessage);
    }

    // endregion

    // region getLastPrices tests

    @Test
    void getLastPrices_returnsPricesInProperOrder() {
        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.APPLE.share().figi();
        final String figi3 = TestShares.YANDEX.share().figi();

        final BigDecimal price1 = DecimalUtils.setDefaultScale(111.111);
        final BigDecimal price2 = DecimalUtils.setDefaultScale(222.222);
        final BigDecimal price3 = DecimalUtils.setDefaultScale(333.333);

        final List<String> figies = List.of(figi1, figi2, figi3);

        final Map<String, BigDecimal> figiesToPrices = new LinkedHashMap<>(3, 1);
        figiesToPrices.put(figi1, price1);
        figiesToPrices.put(figi2, price2);
        figiesToPrices.put(figi3, price3);
        Mocker.mockLastPricesBigDecimal(marketDataService, figiesToPrices);

        final Map<String, BigDecimal> actualResult = extMarketDataService.getLastPrices(figies);

        AssertUtils.assertEquals(figiesToPrices.entrySet(), actualResult.entrySet());
    }

    @Test
    void getLastPrices_throwsInstrumentNotFoundException_whenPriceNotFound() {
        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.APPLE.share().figi();
        final String figi3 = TestShares.YANDEX.share().figi();
        final String figi4 = TestShares.DIOD.share().figi();

        final double price1 = 111.111;
        final double price2 = 222.222;
        final double price3 = 333.333;

        final List<String> figies = List.of(figi1, figi2, figi3, figi4);

        final LastPrice lastPrice1 = TestData.newLastPrice(figi1, price1);
        final LastPrice lastPrice2 = TestData.newLastPrice(figi2, price2);
        final LastPrice lastPrice3 = TestData.newLastPrice(figi3, price3);

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(List.of(lastPrice1, lastPrice2, lastPrice3));

        final Executable executable = () -> extMarketDataService.getLastPrices(figies);
        final String expectedMessage = "Instrument not found for id " + figi4;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    @Test
    void getLastPrices_throwsMultipleInstrumentsFoundException_whenMultiplePricesForSingleFigi() {
        final String figi1 = TestShares.SBER.share().figi();
        final String figi2 = TestShares.APPLE.share().figi();
        final String figi3 = TestShares.YANDEX.share().figi();

        final double price1 = 111.111;
        final double price2 = 222.222;
        final double price3 = 333.333;

        final List<String> figies = List.of(figi1, figi2, figi3);

        final LastPrice lastPrice1 = TestData.newLastPrice(figi1, price1);
        final LastPrice lastPrice2 = TestData.newLastPrice(figi2, price2);
        final LastPrice lastPrice3 = TestData.newLastPrice(figi3, price3);
        final LastPrice lastPrice4 = TestData.newLastPrice(figi1, price3);

        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(List.of(lastPrice1, lastPrice2, lastPrice3, lastPrice4));

        final Executable executable = () -> extMarketDataService.getLastPrices(figies);
        final String expectedMessage = "Multiple instruments found for id " + figi1;
        AssertUtils.assertThrowsWithMessage(MultipleInstrumentsFoundException.class, executable, expectedMessage);
    }

    // endregion

    // region getMarketCandles tests

    @Test
    @DirtiesContext
    void getMarketCandles_returnsMappedCandles() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        final int open1 = 1000;
        final int close1 = 1500;
        final int high1 = 2000;
        final int low1 = 500;
        final OffsetDateTime time1 = from.plusMinutes(1);
        final HistoricCandle historicCandle1 = new HistoricCandleBuilder()
                .setOpen(open1)
                .setClose(close1)
                .setHigh(high1)
                .setLow(low1)
                .setTime(time1)
                .setIsComplete(true)
                .build();

        final int open2 = 1500;
        final int close2 = 2000;
        final int high2 = 2500;
        final int low2 = 1000;
        final OffsetDateTime time2 = from.plusMinutes(2);
        final HistoricCandle historicCandle2 = new HistoricCandleBuilder()
                .setOpen(open2)
                .setClose(close2)
                .setHigh(high2)
                .setLow(low2)
                .setTime(time2)
                .setIsComplete(true)
                .build();

        final int open3 = 2000;
        final int close3 = 2500;
        final int high3 = 3000;
        final int low3 = 500;
        final OffsetDateTime time3 = from.plusMinutes(3);
        final HistoricCandle historicCandle3 = new HistoricCandleBuilder()
                .setOpen(open3)
                .setClose(close3)
                .setHigh(high3)
                .setLow(low3)
                .setTime(time3)
                .setIsComplete(false)
                .build();

        new CandleMocker(marketDataService, figi, candleInterval)
                .add(historicCandle1, historicCandle2, historicCandle3)
                .mock();

        final List<Candle> candles = extMarketDataService.getMarketCandles(figi, interval, candleInterval);

        Assertions.assertEquals(2, candles.size());
        final Candle expectedCandle1 = new CandleBuilder()
                .setOpen(open1)
                .setClose(close1)
                .setHigh(high1)
                .setLow(low1)
                .setTime(time1)
                .build();

        final Candle expectedCandle2 = new CandleBuilder()
                .setOpen(open2)
                .setClose(close2)
                .setHigh(high2)
                .setLow(low2)
                .setTime(time2)
                .build();

        Assertions.assertEquals(expectedCandle1, candles.get(0));
        Assertions.assertEquals(expectedCandle2, candles.get(1));
    }

    @Test
    @DirtiesContext
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() {
        final String figi = TestShares.APPLE.share().figi();
        final OffsetDateTime from = DateTimeTestData.newDateTime(2021, 1, 1, 10);
        final OffsetDateTime to = DateTimeTestData.newDateTime(2021, 1, 2);
        final Interval interval = Interval.of(from, to);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        new CandleMocker(marketDataService, figi, candleInterval)
                .mock();

        final List<Candle> candles = extMarketDataService.getMarketCandles(figi, interval, candleInterval);

        Assertions.assertTrue(candles.isEmpty());
    }

    // endregion

    @Test
    void getTradingStatus_returnsTradingStatus_whenInstrumentExists() {
        final String figi = TestShares.APPLE.share().figi();

        final SecurityTradingStatus status = SecurityTradingStatus.SECURITY_TRADING_STATUS_OPENING_PERIOD;
        Mocker.mockTradingStatus(marketDataService, figi, status);

        final SecurityTradingStatus result = extMarketDataService.getTradingStatus(figi);

        Assertions.assertEquals(status, result);
    }

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
    void convertCurrencyIntoItself(final TestCurrency testCurrency) {
        final String currencyIsoName = testCurrency.tinkoffCurrency().getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult = extMarketDataService.convertCurrency(currencyIsoName, currencyIsoName, sourceValue);

        AssertUtils.assertEquals(sourceValue, actualResult);
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
    ) {
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = sourceTestCurrency.tinkoffCurrency();
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = targetTestCurrency.tinkoffCurrency();

        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency, targetCurrency);

        final Map<String, Double> figiesToPrices = new LinkedHashMap<>();
        figiesToPrices.put(sourceCurrency.getFigi(), price1);
        figiesToPrices.put(targetCurrency.getFigi(), price2);
        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult1 = extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue);

        AssertUtils.assertEquals(expectedResult, actualResult1);
    }

    @Test
    @DirtiesContext
    void convertCurrency_throwsIllegalArgumentException_whenCurrencyNotFound() {
        final ru.tinkoff.piapi.contract.v1.Currency sourceCurrency = TestCurrencies.USD.tinkoffCurrency();
        final ru.tinkoff.piapi.contract.v1.Currency targetCurrency = TestCurrencies.RUB.tinkoffCurrency();

        Mocker.mockAllCurrencies(instrumentsService, targetCurrency);

        final Map<String, Double> figiesToPrices = new LinkedHashMap<>(2, 1);
        figiesToPrices.put(targetCurrency.getFigi(), 1.0);

        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);

        final Executable executable = () -> extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue);
        final String expectedMessage = "Instrument not found for id " + sourceCurrencyIsoName;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, executable, expectedMessage);
    }

    // endregion

}