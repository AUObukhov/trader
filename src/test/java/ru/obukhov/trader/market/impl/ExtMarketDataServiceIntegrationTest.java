package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
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
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
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
import ru.obukhov.trader.test.utils.model.share.TestShare;
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
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
class ExtMarketDataServiceIntegrationTest extends IntegrationTest {

    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

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
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();

        Mocker.mockInstrument(instrumentsService, instrument);

        final CandleMocker candleMocker = new CandleMocker(marketDataService, figi, candleInterval);
        for (final OffsetDateTime candleTime : candlesTimes) {
            candleMocker.add(candleTime);
        }
        candleMocker.mock();

        try (@SuppressWarnings("unused") final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock = Mocker.mockNow(now)) {
            final List<Candle> candles = extMarketDataService.getCandles(figi, Interval.of(from1, to1), candleInterval);

            Assertions.assertEquals(6, candles.size());
            Assertions.assertEquals(candlesTimes[2], candles.getFirst().getTime());
            Assertions.assertEquals(candlesTimes[3], candles.get(1).getTime());
            Assertions.assertEquals(candlesTimes[4], candles.get(2).getTime());
            Assertions.assertEquals(candlesTimes[5], candles.get(3).getTime());
            Assertions.assertEquals(candlesTimes[6], candles.get(4).getTime());
            Assertions.assertEquals(candlesTimes[7], candles.get(5).getTime());

            // caching test
            Mocker.mockEmptyCandles(marketDataService, figi, candleInterval);
            final List<Candle> cachedCandles = extMarketDataService.getCandles(figi, Interval.of(from2, to2), candleInterval);

            Assertions.assertEquals(5, cachedCandles.size());
            Assertions.assertEquals(candlesTimes[1], cachedCandles.getFirst().getTime());
            Assertions.assertEquals(candlesTimes[2], cachedCandles.get(1).getTime());
            Assertions.assertEquals(candlesTimes[3], cachedCandles.get(2).getTime());
            Assertions.assertEquals(candlesTimes[4], cachedCandles.get(3).getTime());
            Assertions.assertEquals(candlesTimes[5], cachedCandles.get(4).getTime());
        }
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirst1MinCandle_whenFromIsNull() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_1_MIN;

        Mocker.mockInstrument(instrumentsService, instrument);

        final OffsetDateTime first1MinCandleDate = instrument.getFirst1MinCandleDate();
        final OffsetDateTime to = first1MinCandleDate.plusMinutes(1);
        new CandleMocker(marketDataService, figi, candleInterval)
                .add(1, first1MinCandleDate.minusNanos(1))
                .add(2, first1MinCandleDate)
                .add(3, to.minusNanos(1))
                .add(4, to)
                .mock();

        final List<Candle> candles = getCandlesAndTestCaching(figi, Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
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
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();

        Mocker.mockInstrument(instrumentsService, instrument);

        final OffsetDateTime first1MinCandleDate = instrument.getFirst1MinCandleDate();
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
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
        AssertUtils.assertEquals(4, candles.get(2).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirst1DayCandle_whenFromIsNull() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_DAY;

        Mocker.mockInstrument(instrumentsService, instrument);

        final OffsetDateTime first1DayCandleDate = instrument.getFirst1DayCandleDate();
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
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
        AssertUtils.assertEquals(4, candles.get(2).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstWeekCandle_whenFromIsNull_andFirst1DayCandleDateIsFirstNanoOfWeek() {
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2020, 7, 13, 10);
        final TestInstrument instrument = TestInstruments.APPLE.withFirst1DayCandleDate(first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_WEEK;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, instrument.getFigi(), candleInterval)
                .add(1, DateTimeTestData.newDateTime(2020, 7, 6, 10))
                .add(2, DateTimeTestData.newDateTime(2020, 7, 13, 10))
                .add(3, DateTimeTestData.newDateTime(2020, 7, 20, 10))
                .add(4, DateTimeTestData.newDateTime(2020, 7, 27, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 7, 27, 10);

        final List<Candle> candles = getCandlesAndTestCaching(instrument.getFigi(), Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstWeekCandle_whenFromIsNull_andFirst1DayCandleDateIsMiddleOfWeek() {
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2020, 7, 17, 10);
        final TestInstrument instrument = TestInstruments.APPLE.withFirst1DayCandleDate(first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_WEEK;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, instrument.getFigi(), candleInterval)
                .add(1, DateTimeTestData.newDateTime(2020, 7, 6, 10))
                .add(2, DateTimeTestData.newDateTime(2020, 7, 13, 10))
                .add(3, DateTimeTestData.newDateTime(2020, 7, 20, 10))
                .add(4, DateTimeTestData.newDateTime(2020, 7, 27, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 7, 27, 10);

        final List<Candle> candles = getCandlesAndTestCaching(instrument.getFigi(), Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstWeekCandle_whenFromIsNull_andFirst1DayCandleDateIsLastNanoOfWeek() {
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2020, 7, 20, 2, 59, 59, 999_999_999);
        final TestInstrument instrument = TestInstruments.APPLE.withFirst1DayCandleDate(first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_WEEK;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, instrument.getFigi(), candleInterval)
                .add(1, DateTimeTestData.newDateTime(2020, 7, 6, 10))
                .add(2, DateTimeTestData.newDateTime(2020, 7, 13, 10))
                .add(3, DateTimeTestData.newDateTime(2020, 7, 20, 10))
                .add(4, DateTimeTestData.newDateTime(2020, 7, 27, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2020, 7, 27, 10);

        final List<Candle> candles = getCandlesAndTestCaching(instrument.getFigi(), Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstMonthCandle_whenFromIsNull_andFirst1DayCandleDateIsFirstNanoOfMonth() {
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2010, 2, 1, 3);
        final TestInstrument instrument = TestInstruments.APPLE.withFirst1DayCandleDate(first1DayCandleDate);

        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_MONTH;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, instrument.getFigi(), candleInterval)
                .add(1, DateTimeTestData.newDateTime(2010, 1, 1, 10))
                .add(2, DateTimeTestData.newDateTime(2010, 2, 1, 10))
                .add(3, DateTimeTestData.newDateTime(2010, 3, 1, 10))
                .add(4, DateTimeTestData.newDateTime(2010, 4, 1, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2010, 4, 1, 10);

        final List<Candle> candles = getCandlesAndTestCaching(instrument.getFigi(), Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstMonthCandle_whenFromIsNull_andFirst1DayCandleDateIsMiddleOfMonth() {
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2010, 2, 10, 10);
        final TestInstrument instrument = TestInstruments.APPLE.withFirst1DayCandleDate(first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_MONTH;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, instrument.getFigi(), candleInterval)
                .add(1, DateTimeTestData.newDateTime(2010, 1, 1, 10))
                .add(2, DateTimeTestData.newDateTime(2010, 2, 1, 10))
                .add(3, DateTimeTestData.newDateTime(2010, 3, 1, 10))
                .add(4, DateTimeTestData.newDateTime(2010, 4, 1, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2010, 4, 1, 10);

        final List<Candle> candles = getCandlesAndTestCaching(instrument.getFigi(), Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
        AssertUtils.assertEquals(3, candles.get(1).getClose());
    }

    @Test
    @DirtiesContext
    void getCandles_adjustsFromByFirstMonthCandle_whenFromIsNull_andFirst1DayCandleDateIsLastNanoOfMonth() {
        final OffsetDateTime first1DayCandleDate = DateTimeTestData.newDateTime(2010, 3, 1, 2, 59, 59, 999_999_999);
        final TestInstrument instrument = TestInstruments.APPLE.withFirst1DayCandleDate(first1DayCandleDate);
        final CandleInterval candleInterval = CandleInterval.CANDLE_INTERVAL_MONTH;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, instrument.getFigi(), candleInterval)
                .add(1, DateTimeTestData.newDateTime(2010, 1, 1, 10))
                .add(2, DateTimeTestData.newDateTime(2010, 2, 1, 10))
                .add(3, DateTimeTestData.newDateTime(2010, 3, 1, 10))
                .add(4, DateTimeTestData.newDateTime(2010, 4, 1, 10))
                .mock();

        final OffsetDateTime to = DateTimeTestData.newDateTime(2010, 4, 1, 10);

        final List<Candle> candles = getCandlesAndTestCaching(instrument.getFigi(), Interval.of(null, to), candleInterval);

        Assertions.assertEquals(2, candles.size());
        AssertUtils.assertEquals(2, candles.getFirst().getClose());
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

    // region getPrice by figi test

    @Test
    @DirtiesContext
    void getPrice_byFigi_returnsClosePrice_whenDateTimeAfterFirst1MinCandle() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(2);
        final int close = 125;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(figi, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_returnsOpenPrice_whenDateTimeWithinFirst1MinCandleInterval() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusSeconds(30);
        final int open = 10;

        Mocker.mockInstrument(instrumentsService, instrument);

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(historicCandle)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(figi, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(open, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_returnsOpenPrice_whenDateTimeEqualToFirst1MinCandleEndTime() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(1);
        final int close = 125;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(figi, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_returnsClosePrice_whenDateTimeAfterFirst1DayCandle() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1DayCandleDate().plusDays(3));
        final OffsetDateTime dateTime = candlesFrom.plusDays(2);
        final int close = 125;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(figi, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_returnsOpenPrice_whenDateTimeWithinFirst1DayCandleInterval() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusHours(10);
        final int open = 10;

        Mocker.mockInstrument(instrumentsService, instrument);

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(historicCandle)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(figi, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(open, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_returnsOpenPrice_whenDateTimeEqualToFirst1DayCandleEndTime() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1DayCandleDate()).plusDays(1);
        final OffsetDateTime dateTime = candlesFrom.plusDays(1);
        final int close = 125;

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(figi, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_throwsInstrumentNotFoundException_whenInstrumentNotFound() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime dateTime = instrument.getFirst1DayCandleDate().minusDays(1);
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(instrument.getFirst1DayCandleDate().plusDays(1));

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(125, candlesFrom)
                .mock();

        final String expectedMessage = "Instrument not found for id " + figi;
        AssertUtils.assertThrowsWithMessage(InstrumentNotFoundException.class, () -> extMarketDataService.getPrice(figi, dateTime), expectedMessage);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_throwsIllegalArgumentException_whenDateTimeBeforeAllCandles() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime dateTime = instrument.getFirst1DayCandleDate().minusDays(1);
        final OffsetDateTime candlesFrom = instrument.getFirst1DayCandleDate().minusHours(1);

        Mocker.mockInstrument(instrumentsService, instrument);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(125, candlesFrom)
                .mock();

        final String expectedMessage = "No candles found for FIGI " + figi + " before " + dateTime;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extMarketDataService.getPrice(figi, dateTime), expectedMessage);
    }

    @Test
    @DirtiesContext
    void getPrice_byFigi_throwsIllegalArgumentException_whenNoCandlesFound() {
        final TestInstrument instrument = TestInstruments.APPLE;
        final String figi = instrument.getFigi();
        final OffsetDateTime dateTime = DateTimeTestData.newEndOfDay(2020, 1, 10);

        Mocker.mockInstrument(instrumentsService, instrument);

        final String expectedMessage = "No candles found for FIGI " + figi + " before " + dateTime;
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, () -> extMarketDataService.getPrice(figi, dateTime), expectedMessage);
    }

    // endregion

    // region getPrice by currency tests

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_returnsClosePrice_whenDateTimeAfterFirst1MinCandle() {
        final Currency currency = TestCurrencies.USD.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(2);
        final int close = 90;

        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_returnsOpenPrice_whenDateTimeWithinFirst1MinCandle() {
        final Currency currency = TestCurrencies.USD.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusSeconds(30);
        final int open = 90;

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(historicCandle)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(open, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_returnsOpenPrice_whenDateTimeEqualToFirst1MinCandleEndTime() {
        final Currency currency = TestCurrencies.USD.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(1);
        final int close = 90;

        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_returnsClosePrice_whenDateTimeAfterFirst1DayCandle() {
        final Currency currency = TestCurrencies.USD.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusDays(2);
        final int close = 90;

        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_returnsOpenPrice_whenDateTimeWithinFirst1DayCandle() {
        final Currency currency = TestCurrencies.USD.currency();
        final String figi = currency.figi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusHours(10);
        final int open = 90;

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(historicCandle)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(open, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_returnsOpenPrice_whenDateTimeEqualToFirst1DayCandleEndTime() {
        final Currency currency = TestCurrencies.USD.currency();
        final String figi = currency.figi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate()).plusDays(1);
        final OffsetDateTime dateTime = candlesFrom.plusDays(1);
        final int close = 90;

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(close, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_throwsIllegalArgumentException_whenDateTimeBeforeAllCandles() {
        final Currency currency = TestCurrencies.USD.currency();
        final String figi = currency.figi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = currency.first1DayCandleDate().minusDays(1);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(90, candlesFrom)
                .mock();

        final String expectedMessage = "No candles found for FIGI " + figi + " before " + dateTime;
        final Executable executable = () -> extMarketDataService.getPrice(currency, dateTime);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getPrice_byUsdCurrency_throwsIllegalArgumentException_whenNoCandlesFound() {
        final Currency currency = TestCurrencies.USD.currency();
        final String figi = currency.figi();
        final OffsetDateTime dateTime = currency.first1DayCandleDate();

        final String expectedMessage = "No candles found for FIGI " + figi + " before " + dateTime;
        final Executable executable = () -> extMarketDataService.getPrice(currency, dateTime);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeAfterFirst1MinCandle() {
        final Currency currency = TestCurrencies.RUB.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(2);
        final int close = 2;

        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeWithinFirst1MinCandle() {
        final Currency currency = TestCurrencies.RUB.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusSeconds(30);
        final int open = 2;

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(historicCandle)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeEqualToFirst1MinCandleEndTime() {
        final Currency currency = TestCurrencies.RUB.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(1);
        final int close = 2;

        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeAfterFirst1DayCandle() {
        final Currency currency = TestCurrencies.RUB.currency();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusDays(2);
        final int close = 2;

        new CandleMocker(marketDataService, currency.figi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeWithinFirst1DayCandle() {
        final Currency currency = TestCurrencies.RUB.currency();
        final String figi = currency.figi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusHours(10);
        final int open = 2;

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(open)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(historicCandle)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeEqualToFirst1DayCandleEndTime() {
        final Currency currency = TestCurrencies.RUB.currency();
        final String figi = currency.figi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate()).plusDays(1);
        final OffsetDateTime dateTime = candlesFrom.plusDays(1);
        final int close = 2;

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(close, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenDateTimeBeforeAllCandles() {
        final Currency currency = TestCurrencies.RUB.currency();
        final String figi = currency.figi();
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(currency.first1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = currency.first1DayCandleDate().minusDays(1);

        new CandleMocker(marketDataService, figi, CandleInterval.CANDLE_INTERVAL_DAY)
                .add(2, candlesFrom)
                .mock();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    @Test
    @DirtiesContext
    void getPrice_byRubCurrency_returnsOne_whenNoCandlesFound() {
        final Currency currency = TestCurrencies.RUB.currency();
        final OffsetDateTime dateTime = currency.first1DayCandleDate();

        final BigDecimal price = extMarketDataService.getPrice(currency, dateTime);

        Assertions.assertNotNull(price);
        AssertUtils.assertEquals(1, price);
    }

    // endregion

    // region getLastPrices tests

    @Test
    void getLastPrices_returnsPricesInProperOrder() {
        final String figi1 = TestShares.SBER.getFigi();
        final String figi2 = TestShares.APPLE.getFigi();
        final String figi3 = TestShares.YANDEX.getFigi();

        final BigDecimal price1 = DecimalUtils.setDefaultScale(111.111);
        final BigDecimal price2 = DecimalUtils.setDefaultScale(222.222);
        final BigDecimal price3 = DecimalUtils.setDefaultScale(333.333);

        final List<String> figies = List.of(figi1, figi2, figi3);

        final SequencedMap<String, BigDecimal> figiesToPrices = new LinkedHashMap<>(3, 1);
        figiesToPrices.put(figi1, price1);
        figiesToPrices.put(figi2, price2);
        figiesToPrices.put(figi3, price3);
        Mocker.mockLastPricesBigDecimal(marketDataService, figiesToPrices);

        final SequencedMap<String, BigDecimal> actualResult = extMarketDataService.getLastPrices(figies);

        AssertUtils.assertEquals((SequencedCollection<?>) figiesToPrices.entrySet(), (SequencedCollection<?>) actualResult.entrySet());
    }

    @Test
    void getLastPrices_throwsInstrumentNotFoundException_whenPriceNotFound() {
        final String figi1 = TestShares.SBER.getFigi();
        final String figi2 = TestShares.APPLE.getFigi();
        final String figi3 = TestShares.YANDEX.getFigi();
        final String figi4 = TestShares.DIOD.getFigi();

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
        final String figi1 = TestShares.SBER.getFigi();
        final String figi2 = TestShares.APPLE.getFigi();
        final String figi3 = TestShares.YANDEX.getFigi();

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

    // region getSharesPrices tests

    @Test
    @DirtiesContext
    void getSharesPrices_returnsClosePrice_whenDateTimeAfterFirst1MinCandle() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.WOOSH;

        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(share2.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(2);
        final int closePrice2 = 340;

        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(closePrice2, candlesFrom)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final Map<String, BigDecimal> prices = extMarketDataService.getSharesPrices(shares, dateTime);

        final Map<String, BigDecimal> expectedPrices = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(QUOTATION_MAPPER.toBigDecimal(lastPrices.get(0).getPrice())),
                share2.getFigi(), DecimalUtils.setDefaultScale(closePrice2)
        );
        AssertUtils.assertEquals(expectedPrices, prices);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_returnsOpenPrice_whenDateTimeWithinFirst1MinCandleInterval() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(share2.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusSeconds(30);
        final int openPrice2 = 10;

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(openPrice2)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(historicCandle)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final Map<String, BigDecimal> prices = extMarketDataService.getSharesPrices(shares, dateTime);

        final Map<String, BigDecimal> expectedPrices = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(QUOTATION_MAPPER.toBigDecimal(lastPrices.get(0).getPrice())),
                share2.getFigi(), DecimalUtils.setDefaultScale(openPrice2)
        );
        AssertUtils.assertEquals(expectedPrices, prices);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_returnsOpenPrice_whenDateTimeEqualToFirst1MinCandleEndTime() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(share2.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(1);
        final int closePrice2 = 125;

        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(closePrice2, candlesFrom)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final Map<String, BigDecimal> prices = extMarketDataService.getSharesPrices(shares, dateTime);

        final Map<String, BigDecimal> expectedPrices = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(QUOTATION_MAPPER.toBigDecimal(lastPrices.get(0).getPrice())),
                share2.getFigi(), DecimalUtils.setDefaultScale(closePrice2)
        );
        AssertUtils.assertEquals(expectedPrices, prices);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_returnsClosePrice_whenDateTimeAfterFirst1DayCandle() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(share2.getFirst1DayCandleDate().plusDays(3));
        final OffsetDateTime dateTime = candlesFrom.plusDays(2);
        final int closePrice2 = 125;

        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(closePrice2, candlesFrom)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final Map<String, BigDecimal> prices = extMarketDataService.getSharesPrices(shares, dateTime);

        final Map<String, BigDecimal> expectedPrices = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(QUOTATION_MAPPER.toBigDecimal(lastPrices.get(0).getPrice())),
                share2.getFigi(), DecimalUtils.setDefaultScale(closePrice2)
        );
        AssertUtils.assertEquals(expectedPrices, prices);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_returnsOpenPrice_whenDateTimeWithinFirst1DayCandleInterval() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(share2.getFirst1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusHours(10);
        final int openPrice2 = 10;

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(openPrice2)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(historicCandle)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final Map<String, BigDecimal> prices = extMarketDataService.getSharesPrices(shares, dateTime);

        final Map<String, BigDecimal> expectedPrices = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(QUOTATION_MAPPER.toBigDecimal(lastPrices.get(0).getPrice())),
                share2.getFigi(), DecimalUtils.setDefaultScale(openPrice2)
        );
        AssertUtils.assertEquals(expectedPrices, prices);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_returnsOpenPrice_whenDateTimeEqualToFirst1DayCandleEndTime() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(share2.getFirst1DayCandleDate()).plusDays(1);
        final OffsetDateTime dateTime = candlesFrom.plusDays(1);
        final int closePrice2 = 125;

        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(closePrice2, candlesFrom)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final Map<String, BigDecimal> prices = extMarketDataService.getSharesPrices(shares, dateTime);

        final Map<String, BigDecimal> expectedPrices = Map.of(
                share1.getFigi(), DecimalUtils.setDefaultScale(QUOTATION_MAPPER.toBigDecimal(lastPrices.get(0).getPrice())),
                share2.getFigi(), DecimalUtils.setDefaultScale(closePrice2)
        );
        AssertUtils.assertEquals(expectedPrices, prices);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_throwsIllegalArgumentException_whenDateTimeBeforeAllCandles() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final OffsetDateTime dateTime = share2.getFirst1DayCandleDate().minusDays(1);
        final OffsetDateTime candlesFrom = share2.getFirst1DayCandleDate().minusHours(1);

        new CandleMocker(marketDataService, share2.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(125, candlesFrom)
                .mock();

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, candlesFrom.minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final String expectedMessage = "No candles found for FIGI " + share2.getFigi() + " before " + dateTime;
        final Executable executable = () -> extMarketDataService.getSharesPrices(shares, dateTime);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void getSharesPrices_throwsIllegalArgumentException_whenNoCandlesFound() {
        final TestShare share1 = TestShares.SPB_BANK;
        final TestShare share2 = TestShares.APPLE;

        final List<TestShare> testShares = List.of(share1, share2);
        final List<String> figies = testShares.stream().map(TestShare::getFigi).toList();
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(share1.getFigi(), 100, share2.getFirst1DayCandleDate().minusDays(1)),
                share2.getLastPrice()
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        final OffsetDateTime dateTime = DateTimeTestData.newEndOfDay(2020, 1, 10);

        final List<Share> shares = testShares.stream().map(TestShare::share).toList();
        final String expectedMessage = "No candles found for FIGI " + share2.getFigi() + " before " + dateTime;
        final Executable executable = () -> extMarketDataService.getSharesPrices(shares, dateTime);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

    // region getMarketCandles tests

    @Test
    @DirtiesContext
    void getMarketCandles_returnsMappedCandles() {
        final String figi = TestShares.APPLE.getFigi();
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

        Assertions.assertEquals(expectedCandle1, candles.getFirst());
        Assertions.assertEquals(expectedCandle2, candles.get(1));
    }

    @Test
    @DirtiesContext
    void getMarketCandles_returnsEmptyList_whenGetsNoCandles() {
        final String figi = TestShares.APPLE.getFigi();
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
        final String figi = TestShares.APPLE.getFigi();

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
        final String currencyIsoName = testCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult = extMarketDataService.convertCurrency(currencyIsoName, currencyIsoName, sourceValue);

        AssertUtils.assertEquals(sourceValue, actualResult);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forConvertCurrency() {
        return Stream.of(
                Arguments.of(TestCurrencies.RUB, TestCurrencies.RUB, 1, 1, 1),
                Arguments.of(TestCurrencies.USD, TestCurrencies.USD, 97.31, 97.31, 1),
                Arguments.of(TestCurrencies.USD, TestCurrencies.RUB, 97.31, 1, 97.310),
                Arguments.of(TestCurrencies.RUB, TestCurrencies.USD, 1, 97.31, 0.010276436132),
                Arguments.of(TestCurrencies.USD, TestCurrencies.CNY, 97.31, 13.322, 7.304458789971),
                Arguments.of(TestCurrencies.CNY, TestCurrencies.USD, 13.322, 97.31, 0.13690268215)
        );
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyPrice,
            final double expectedResult
    ) {
        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency, targetCurrency);

        final SequencedMap<String, Double> figiesToPrices = new LinkedHashMap<>();
        figiesToPrices.put(sourceCurrency.getFigi(), sourceCurrencyPrice);
        figiesToPrices.put(targetCurrency.getFigi(), targetCurrencyPrice);
        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult = extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue);

        AssertUtils.assertEquals(DecimalUtils.multiply(sourceValue, expectedResult), actualResult);
    }

    @Test
    @DirtiesContext
    void convertCurrency_throwsIllegalArgumentException_whenCurrencyNotFound() {
        final TestCurrency sourceCurrency = TestCurrencies.USD;
        final TestCurrency targetCurrency = TestCurrencies.RUB;

        Mocker.mockAllCurrencies(instrumentsService, targetCurrency);

        final SequencedMap<String, Double> figiesToPrices = new LinkedHashMap<>(2, 1);
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

    // region convertCurrency with date tests

    @ParameterizedTest
    @MethodSource("getData_forConvertCurrencyIntoItself")
    void convertCurrency_withDateTime_intoItself(final TestCurrency testCurrency) {
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2024, 4, 1);
        testConvertCurrency(testCurrency, testCurrency, 1, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_whenDateTimeAfterLastPrice(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyPrice,
            final double expectedResult
    ) {
        final SequencedMap<String, Double> figiesToPrices = new LinkedHashMap<>();
        figiesToPrices.put(sourceCurrency.getFigi(), sourceCurrencyPrice);
        figiesToPrices.put(targetCurrency.getFigi(), targetCurrencyPrice);
        Mocker.mockLastPricesDouble(marketDataService, figiesToPrices);

        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2024, 4, 1);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_usesClosePrice_whenDateTimeAfterFirst1MinCandle(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyClosePrice,
            final double expectedResult
    ) {
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(targetCurrency.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(2);

        new CandleMocker(marketDataService, targetCurrency.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(targetCurrencyClosePrice, candlesFrom)
                .mock();

        final List<String> figies = List.of(sourceCurrency.getFigi(), targetCurrency.getFigi());
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(sourceCurrency.getFigi(), sourceCurrencyPrice, candlesFrom.minusDays(1)),
                TestData.newLastPrice(targetCurrency.getFigi(), 0, dateTime)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_usesOpenPrice_whenDateTimeWithinFirst1MinCandleInterval(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyOpenPrice,
            final double expectedResult
    ) {
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(targetCurrency.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusSeconds(30);

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(targetCurrencyOpenPrice)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, targetCurrency.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(historicCandle)
                .mock();

        final List<String> figies = List.of(sourceCurrency.getFigi(), targetCurrency.getFigi());
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(sourceCurrency.getFigi(), sourceCurrencyPrice, candlesFrom.minusDays(1)),
                TestData.newLastPrice(targetCurrency.getFigi(), 0, dateTime)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_usesOpenPrice_whenDateTimeEqualToFirst1MinCandleEndTime(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyClosePrice,
            final double expectedResult
    ) {
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(targetCurrency.getFirst1MinCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusMinutes(1);

        new CandleMocker(marketDataService, targetCurrency.getFigi(), CandleInterval.CANDLE_INTERVAL_1_MIN)
                .add(targetCurrencyClosePrice, candlesFrom)
                .mock();

        final List<String> figies = List.of(sourceCurrency.getFigi(), targetCurrency.getFigi());
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(sourceCurrency.getFigi(), sourceCurrencyPrice, candlesFrom.minusDays(1)),
                TestData.newLastPrice(targetCurrency.getFigi(), 0, dateTime)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_usesClosePrice_whenDateTimeAfterFirst1DayCandle(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyClosePrice,
            final double expectedResult
    ) {
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(targetCurrency.getFirst1DayCandleDate().plusDays(3));
        final OffsetDateTime dateTime = candlesFrom.plusDays(2);

        new CandleMocker(marketDataService, targetCurrency.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(targetCurrencyClosePrice, candlesFrom)
                .mock();

        final List<String> figies = List.of(sourceCurrency.getFigi(), targetCurrency.getFigi());
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(sourceCurrency.getFigi(), sourceCurrencyPrice, candlesFrom.minusDays(1)),
                TestData.newLastPrice(targetCurrency.getFigi(), 0, dateTime)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_usesOpenPrice_whenDateTimeWithinFirst1DayCandleInterval(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyOpenPrice,
            final double expectedResult
    ) {
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(targetCurrency.getFirst1DayCandleDate().plusDays(1));
        final OffsetDateTime dateTime = candlesFrom.plusHours(10);

        final HistoricCandle historicCandle = new HistoricCandleBuilder()
                .setOpen(targetCurrencyOpenPrice)
                .setTime(candlesFrom)
                .setIsComplete(true)
                .build();
        new CandleMocker(marketDataService, targetCurrency.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(historicCandle)
                .mock();

        final List<String> figies = List.of(sourceCurrency.getFigi(), targetCurrency.getFigi());
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(sourceCurrency.getFigi(), sourceCurrencyPrice, candlesFrom.minusDays(1)),
                TestData.newLastPrice(targetCurrency.getFigi(), 0, dateTime)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    @DirtiesContext
    @ParameterizedTest
    @MethodSource("getData_forConvertCurrency")
    void convertCurrency_withDateTime_usesOpenPrice_whenDateTimeEqualToFirst1DayCandleEndTime(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double sourceCurrencyPrice,
            final double targetCurrencyClosePrice,
            final double expectedResult
    ) {
        final OffsetDateTime candlesFrom = DateUtils.toStartOfDay(targetCurrency.getFirst1DayCandleDate()).plusDays(1);
        final OffsetDateTime dateTime = candlesFrom.plusDays(1);

        new CandleMocker(marketDataService, targetCurrency.getFigi(), CandleInterval.CANDLE_INTERVAL_DAY)
                .add(targetCurrencyClosePrice, candlesFrom)
                .mock();

        final List<String> figies = List.of(sourceCurrency.getFigi(), targetCurrency.getFigi());
        final List<LastPrice> lastPrices = List.of(
                TestData.newLastPrice(sourceCurrency.getFigi(), sourceCurrencyPrice, candlesFrom.minusDays(1)),
                TestData.newLastPrice(targetCurrency.getFigi(), 0, dateTime)
        );
        Mockito.when(marketDataService.getLastPricesSync(figies)).thenReturn(lastPrices);

        testConvertCurrency(sourceCurrency, targetCurrency, expectedResult, dateTime);
    }

    private void testConvertCurrency(
            final TestCurrency sourceCurrency,
            final TestCurrency targetCurrency,
            final double expectedResult,
            final OffsetDateTime dateTime
    ) {
        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency, targetCurrency);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final BigDecimal actualResult = extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue, dateTime);

        AssertUtils.assertEquals(DecimalUtils.multiply(sourceValue, expectedResult), actualResult);
    }

    @Test
    @DirtiesContext
    void convertCurrency_withDateTime_throwsIllegalArgumentException_whenSourceCurrencyNotFound() {
        final TestCurrency sourceCurrency = TestCurrencies.USD;
        final TestCurrency targetCurrency = TestCurrencies.RUB;

        Mocker.mockAllCurrencies(instrumentsService, targetCurrency);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2024, 4, 1);

        final Executable executable = () -> extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue, dateTime);
        final String expectedMessage = "Expected single item. No items found.";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void convertCurrency_withDateTime_throwsIllegalArgumentException_whenTargetCurrencyNotFound() {
        final TestCurrency sourceCurrency = TestCurrencies.USD;
        final TestCurrency targetCurrency = TestCurrencies.RUB;

        Mocker.mockAllCurrencies(instrumentsService, sourceCurrency);

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2024, 4, 1);

        final Executable executable = () -> extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue, dateTime);
        final String expectedMessage = "Expected single item. No items found.";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    @DirtiesContext
    void convertCurrency_withDateTime_throwsIllegalArgumentException_whenBothCurrenciesNotFound() {
        final TestCurrency sourceCurrency = TestCurrencies.USD;
        final TestCurrency targetCurrency = TestCurrencies.RUB;

        final String sourceCurrencyIsoName = sourceCurrency.getIsoCurrencyName();
        final String targetCurrencyIsoName = targetCurrency.getIsoCurrencyName();
        final BigDecimal sourceValue = DecimalUtils.setDefaultScale(1000);
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2024, 4, 1);

        final Executable executable = () -> extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue, dateTime);
        final String expectedMessage = "Expected single item. No items found.";
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    // endregion

}