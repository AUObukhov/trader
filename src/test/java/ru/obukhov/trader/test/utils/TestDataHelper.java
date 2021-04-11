package ru.obukhov.trader.test.utils;

import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.web.model.pojo.SimulationUnit;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.InstrumentType;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.MoneyAmount;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TestDataHelper {

    public static ru.tinkoff.invest.openapi.model.rest.Candle createTinkoffCandle(
            double openPrice,
            double closePrice,
            double highestPrice,
            double lowestPrice
    ) {
        return createTinkoffCandle(CandleResolution.DAY, openPrice, closePrice, highestPrice, lowestPrice);
    }

    public static ru.tinkoff.invest.openapi.model.rest.Candle createTinkoffCandle(
            CandleResolution interval,
            double openPrice,
            double closePrice,
            double highestPrice,
            double lowestPrice
    ) {
        return createTinkoffCandle(
                interval,
                openPrice,
                closePrice,
                highestPrice,
                lowestPrice,
                OffsetDateTime.now()
        );
    }

    public static ru.tinkoff.invest.openapi.model.rest.Candle createTinkoffCandle(
            CandleResolution interval,
            double openPrice,
            double closePrice,
            double highestPrice,
            double lowestPrice,
            OffsetDateTime time
    ) {
        return new ru.tinkoff.invest.openapi.model.rest.Candle()
                .figi(StringUtils.EMPTY)
                .interval(interval)
                .o(DecimalUtils.setDefaultScale(openPrice))
                .c(DecimalUtils.setDefaultScale(closePrice))
                .h(DecimalUtils.setDefaultScale(highestPrice))
                .l(DecimalUtils.setDefaultScale(lowestPrice))
                .v(0)
                .time(time);
    }

    public static Candle createCandle(
            double openPrice,
            double closePrice,
            double highestPrice,
            double lowestPrice,
            OffsetDateTime time,
            CandleResolution interval
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

    public static Candle createCandleWithTimeAndInterval(OffsetDateTime time, CandleResolution interval) {
        Candle candle = new Candle();
        candle.setTime(time);
        candle.setInterval(interval);
        return candle;
    }

    public static MarketInstrument createAndMockInstrument(TinkoffService tinkoffService, String ticker) {
        MarketInstrument instrument = new MarketInstrument()
                .figi(StringUtils.EMPTY)
                .ticker(ticker)
                .lot(0)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);

        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(MarketService marketService, String ticker) {
        MarketInstrument instrument = new MarketInstrument()
                .figi(StringUtils.EMPTY)
                .ticker(ticker)
                .lot(0)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);

        Mockito.when(marketService.getInstrument(ticker)).thenReturn(instrument);

        return instrument;
    }

    public static ZoneOffset getNotDefaultOffset() {
        ZoneOffset defaultOffset = OffsetDateTime.now().getOffset();
        int totalSeconds = defaultOffset.getTotalSeconds() + (int) TimeUnit.HOURS.toSeconds(1L);
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    public static MockedStatic<OffsetDateTime> mockNow(OffsetDateTime mockedNow) {
        MockedStatic<OffsetDateTime> offsetDateTimeStaticMock =
                Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS);
        offsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);
        return offsetDateTimeStaticMock;
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

    public static DecisionData createDecisionData(double averagePositionPrice, double currentPrice) {
        DecisionData decisionData = new DecisionData();
        decisionData.setPosition(createPortfolioPosition(averagePositionPrice));
        decisionData.setCurrentCandles(Collections.singletonList(createCandleWithOpenPrice(currentPrice)));

        return decisionData;
    }

    public static DecisionData createDecisionData(double balance, double currentPrice, int lotSize) {
        DecisionData decisionData = new DecisionData();
        decisionData.setBalance(DecimalUtils.setDefaultScale(balance));
        decisionData.setCurrentCandles(Collections.singletonList(createCandleWithOpenPrice(currentPrice)));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setInstrument(new MarketInstrument().lot(lotSize));

        return decisionData;
    }

    public static PortfolioPosition createPortfolioPosition(String ticker) {
        return createPortfolioPosition(ticker, 1);
    }

    public static PortfolioPosition createPortfolioPosition(double averagePositionPrice) {
        return new PortfolioPosition(
                StringUtils.EMPTY,
                BigDecimal.ZERO,
                null,
                Currency.RUB,
                null,
                0,
                DecimalUtils.setDefaultScale(averagePositionPrice),
                null,
                StringUtils.EMPTY
        );
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

    public static Operation createTinkoffOperation(
            OffsetDateTime operationDateTime,
            OperationTypeWithCommission operationType,
            BigDecimal operationPrice,
            int operationQuantity,
            BigDecimal commissionValue
    ) {
        return new Operation()
                .id(StringUtils.EMPTY)
                .status(OperationStatus.DONE)
                .commission(new MoneyAmount().value(commissionValue))
                .currency(Currency.RUB)
                .payment(BigDecimal.ZERO)
                .price(operationPrice)
                .quantity(operationQuantity)
                .quantityExecuted(operationQuantity)
                .date(operationDateTime)
                .operationType(operationType);
    }

    public static void mockTinkoffOperations(
            TinkoffService tinkoffService,
            String ticker,
            Interval interval,
            Operation... operations
    ) {
        Mockito.when(tinkoffService.getOperations(interval, ticker))
                .thenReturn(Arrays.asList(operations));
    }

    public static CurrencyPosition createCurrencyPosition(Currency currency, long balance) {
        return new CurrencyPosition()
                .currency(currency)
                .balance(DecimalUtils.setDefaultScale(balance));
    }

    public static CurrencyPosition createCurrencyPosition(Currency currency, long balance, long blocked) {
        return new CurrencyPosition()
                .currency(currency)
                .balance(DecimalUtils.setDefaultScale(balance))
                .blocked(DecimalUtils.setDefaultScale(blocked));
    }

    public static MoneyAmount createMoneyAmount(Currency currency, long value) {
        return new MoneyAmount()
                .currency(currency)
                .value(BigDecimal.valueOf(value));
    }

    public static CompletableFuture<MarketInstrumentList> createInstrumentsFuture(MarketInstrument... instruments) {
        List<MarketInstrument> instrumentList = Arrays.asList(instruments);
        MarketInstrumentList marketInstrumentList = new MarketInstrumentList().instruments(instrumentList);
        return CompletableFuture.completedFuture(marketInstrumentList);
    }

    public static List<BigDecimal> getBigDecimalValues(List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<Optional<BigDecimal>> getOptionalBigDecimalValues(List<Double> values) {
        return values.stream()
                .map(DecimalUtils::setDefaultScale)
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static MockedStatic<Runtime> mockRuntime(Runtime runtime) {
        MockedStatic<Runtime> runtimeStaticMock = Mockito.mockStatic(Runtime.class, Mockito.CALLS_REAL_METHODS);
        runtimeStaticMock.when(Runtime::getRuntime).thenReturn(runtime);
        return runtimeStaticMock;
    }

}