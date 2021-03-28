package ru.obukhov.trader.test.utils;

import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.market.CandleInterval;
import ru.tinkoff.invest.openapi.models.market.Instrument;
import ru.tinkoff.invest.openapi.models.market.InstrumentType;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;
import ru.tinkoff.invest.openapi.models.operations.OperationType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
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

    public static Candle createCandle(
            double openPrice,
            double closePrice,
            double highestPrice,
            double lowestPrice,
            OffsetDateTime time,
            CandleInterval interval
    ) {
        return new Candle(
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(highestPrice)),
                DecimalUtils.setDefaultScale(BigDecimal.valueOf(lowestPrice)),
                time,
                interval
        );
    }

    public static Candle createCandleWithOpenPrice(double openPrice) {
        return createCandleWithOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
    }

    public static Candle createCandleWithOpenPrice(BigDecimal openPrice) {
        Candle candle = new Candle();
        candle.setOpenPrice(openPrice);
        return candle;
    }

    public static Candle createCandleWithClosePrice(double closePrice) {
        Candle candle = new Candle();
        candle.setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
        return candle;
    }

    public static Candle createCandleWithTime(OffsetDateTime time) {
        Candle candle = new Candle();
        candle.setTime(time);
        return candle;
    }

    public static Candle createCandleWithOpenPriceAndTime(double openPrice, OffsetDateTime time) {
        Candle candle = new Candle();
        candle.setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
        candle.setTime(time);
        return candle;
    }

    public static Candle createCandleWithClosePriceAndTime(double closePrice, OffsetDateTime time) {
        Candle candle = new Candle();
        candle.setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
        candle.setTime(time);
        return candle;
    }

    public static Candle createCandleWithTimeAndInterval(OffsetDateTime time, CandleInterval interval) {
        Candle candle = new Candle();
        candle.setTime(time);
        candle.setInterval(interval);
        return candle;
    }

    public static Instrument createAndMockInstrument(TinkoffService tinkoffService, String ticker) {
        Instrument instrument = new Instrument(StringUtils.EMPTY,
                ticker,
                null,
                null,
                0,
                Currency.RUB,
                StringUtils.EMPTY,
                InstrumentType.Stock);

        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        return instrument;
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

    public static SimulationUnit createSimulationUnit(String ticker, BigDecimal initialBalance) {
        SimulationUnit simulationUnit = new SimulationUnit();
        simulationUnit.setTicker(ticker);
        simulationUnit.setInitialBalance(initialBalance);
        return simulationUnit;
    }

    public static DecisionData createDecisionData(Candle... candles) {
        DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(Arrays.asList(candles));
        return decisionData;
    }

    public static PortfolioPosition createPortfolioPosition(String ticker, int lotsCount) {
        return new PortfolioPosition(
                ticker,
                BigDecimal.ZERO,
                null,
                Currency.RUB,
                null,
                lotsCount,
                null,
                null,
                StringUtils.EMPTY
        );
    }

    public static Operation createTinkoffOperation(OffsetDateTime operationDateTime,
                                                   OperationType operationType,
                                                   BigDecimal operationPrice,
                                                   int operationQuantity,
                                                   BigDecimal operationCommission) {
        return new Operation(StringUtils.EMPTY,
                OperationStatus.Done,
                null,
                new MoneyAmount(Currency.RUB, operationCommission),
                Currency.RUB,
                BigDecimal.ZERO,
                operationPrice,
                operationQuantity,
                null,
                null,
                false,
                operationDateTime,
                operationType);
    }

    public static void mockTinkoffOperations(TinkoffService tinkoffService,
                                             String ticker,
                                             Interval interval,
                                             Operation... operations) {
        Mockito.when(tinkoffService.getOperations(interval, ticker))
                .thenReturn(Arrays.asList(operations));
    }

}