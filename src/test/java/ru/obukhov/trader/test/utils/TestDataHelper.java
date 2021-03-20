package ru.obukhov.trader.test.utils;

import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class TestDataHelper {

    public static ru.tinkoff.invest.openapi.models.market.Candle createTinkoffCandle(double openPrice,
                                                                                     double closePrice,
                                                                                     double highestPrice,
                                                                                     double lowestPrice) {

        return new ru.tinkoff.invest.openapi.models.market.Candle(
                StringUtils.EMPTY,
                CandleInterval.DAY,
                BigDecimal.valueOf(openPrice),
                BigDecimal.valueOf(closePrice),
                BigDecimal.valueOf(highestPrice),
                BigDecimal.valueOf(lowestPrice),
                BigDecimal.TEN,
                OffsetDateTime.now()
        );

    }

    public static void createAndMockInstrument(TinkoffService tinkoffService, String ticker) {
        Instrument instrument = new Instrument(StringUtils.EMPTY,
                ticker,
                null,
                null,
                0,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.Stock);

        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);
    }

    public static ZoneOffset getNotDefaultOffset() {
        ZoneOffset defaultOffset = OffsetDateTime.now().getOffset();
        int totalSeconds = defaultOffset.getTotalSeconds() + (int) TimeUnit.HOURS.toSeconds(1L);
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    public static MockedStatic<OffsetDateTime> mockNow(OffsetDateTime mockedNow) {
        MockedStatic<OffsetDateTime> OffsetDateTimeStaticMock =
                Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS);
        OffsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);
        return OffsetDateTimeStaticMock;
    }

}