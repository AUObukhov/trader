package ru.obukhov.trader.test.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.quartz.CronExpression;
import ru.obukhov.trader.bot.model.DecisionData;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
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
import ru.tinkoff.invest.openapi.model.rest.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataHelper {

    public static ru.tinkoff.invest.openapi.model.rest.Candle createTinkoffCandle(
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice
    ) {
        return createTinkoffCandle(CandleResolution.DAY, openPrice, closePrice, highestPrice, lowestPrice);
    }

    public static ru.tinkoff.invest.openapi.model.rest.Candle createTinkoffCandle(
            final CandleResolution interval,
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice
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
            final CandleResolution interval,
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice,
            final OffsetDateTime time
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
            final double openPrice,
            final double closePrice,
            final double highestPrice,
            final double lowestPrice,
            final OffsetDateTime time,
            final CandleResolution interval
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

    public static Candle createCandleWithOpenPrice(final double openPrice) {
        return createCandleWithOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
    }

    public static Candle createCandleWithOpenPrice(final BigDecimal openPrice) {
        final Candle candle = new Candle();
        candle.setOpenPrice(openPrice);
        return candle;
    }

    public static Candle createCandleWithClosePrice(final double closePrice) {
        final Candle candle = new Candle();
        candle.setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
        return candle;
    }

    public static Candle createCandleWithTime(final OffsetDateTime time) {
        final Candle candle = new Candle();
        candle.setTime(time);
        return candle;
    }

    public static Candle createCandleWithOpenPriceAndTime(final double openPrice, final OffsetDateTime time) {
        final Candle candle = new Candle();
        candle.setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
        candle.setTime(time);
        return candle;
    }

    public static Candle createCandleWithClosePriceAndTime(final double closePrice, final OffsetDateTime time) {
        final Candle candle = new Candle();
        candle.setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
        candle.setTime(time);
        return candle;
    }

    public static Candle createCandleWithTimeAndInterval(final OffsetDateTime time, final CandleResolution interval) {
        final Candle candle = new Candle();
        candle.setTime(time);
        candle.setInterval(interval);
        return candle;
    }

    public static MarketInstrument createAndMockInstrument(final TinkoffService tinkoffService, final String ticker) {
        final MarketInstrument instrument = new MarketInstrument()
                .figi(StringUtils.EMPTY)
                .ticker(ticker)
                .lot(0)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);

        Mockito.when(tinkoffService.searchMarketInstrument(ticker)).thenReturn(instrument);

        return instrument;
    }

    public static MarketInstrument createAndMockInstrument(final MarketService marketService, final String ticker) {
        final MarketInstrument instrument = new MarketInstrument()
                .figi(StringUtils.EMPTY)
                .ticker(ticker)
                .lot(0)
                .currency(Currency.RUB)
                .name(StringUtils.EMPTY)
                .type(InstrumentType.STOCK);

        Mockito.when(marketService.getInstrument(ticker)).thenReturn(instrument);

        return instrument;
    }

    public static void mockEmptyOrder(final OrdersService ordersService, final String ticker) {
        final Order order = new Order();
        Mockito.when(ordersService.getOrders(ticker)).thenReturn(List.of(order));
    }

    public static ZoneOffset getNotDefaultOffset() {
        final ZoneOffset defaultOffset = OffsetDateTime.now().getOffset();
        final int totalSeconds = defaultOffset.getTotalSeconds() + (int) TimeUnit.HOURS.toSeconds(1L);
        return ZoneOffset.ofTotalSeconds(totalSeconds);
    }

    public static MockedStatic<OffsetDateTime> mockNow(final OffsetDateTime mockedNow) {
        final MockedStatic<OffsetDateTime> offsetDateTimeStaticMock =
                Mockito.mockStatic(OffsetDateTime.class, Mockito.CALLS_REAL_METHODS);
        offsetDateTimeStaticMock.when(OffsetDateTime::now).thenReturn(mockedNow);
        return offsetDateTimeStaticMock;
    }

    public static DecisionData createDecisionData(final Candle... candles) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(List.of(candles));
        return decisionData;
    }

    public static DecisionData createDecisionData(final double averagePositionPrice, final double currentPrice) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setPosition(createPortfolioPosition(averagePositionPrice));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));

        return decisionData;
    }

    public static DecisionData createDecisionData(
            final double averagePositionPrice,
            final int positionLotsCount,
            final double currentPrice
    ) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setPosition(createPortfolioPosition(averagePositionPrice, positionLotsCount));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));

        return decisionData;
    }

    public static DecisionData createDecisionData(final double balance, final double currentPrice, final int lotSize) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setBalance(DecimalUtils.setDefaultScale(balance));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));
        decisionData.setLastOperations(new ArrayList<>());
        decisionData.setInstrument(new MarketInstrument().lot(lotSize));

        return decisionData;
    }

    public static PortfolioPosition createPortfolioPosition(final String ticker) {
        return createPortfolioPosition(ticker, 1);
    }

    public static PortfolioPosition createPortfolioPosition(final double averagePositionPrice) {
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

    public static PortfolioPosition createPortfolioPosition(final double averagePositionPrice, final int lotsCount) {
        return new PortfolioPosition(
                StringUtils.EMPTY,
                BigDecimal.ZERO,
                null,
                Currency.RUB,
                null,
                lotsCount,
                DecimalUtils.setDefaultScale(averagePositionPrice),
                null,
                StringUtils.EMPTY
        );
    }

    public static PortfolioPosition createPortfolioPosition(final String ticker, final int lotsCount) {
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
            final OffsetDateTime operationDateTime,
            final OperationTypeWithCommission operationType,
            final BigDecimal operationPrice,
            final int operationQuantity,
            final BigDecimal commissionValue
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
            final TinkoffService tinkoffService,
            final String ticker,
            final Interval interval,
            final Operation... operations
    ) {
        Mockito.when(tinkoffService.getOperations(interval, ticker))
                .thenReturn(List.of(operations));
    }

    public static CurrencyPosition createCurrencyPosition(final Currency currency, final long balance) {
        return new CurrencyPosition()
                .currency(currency)
                .balance(DecimalUtils.setDefaultScale(balance));
    }

    public static CurrencyPosition createCurrencyPosition(
            final Currency currency,
            final long balance,
            final long blocked
    ) {
        return new CurrencyPosition()
                .currency(currency)
                .balance(DecimalUtils.setDefaultScale(balance))
                .blocked(DecimalUtils.setDefaultScale(blocked));
    }

    public static MoneyAmount createMoneyAmount(final Currency currency, final long value) {
        return new MoneyAmount()
                .currency(currency)
                .value(BigDecimal.valueOf(value));
    }

    public static CompletableFuture<MarketInstrumentList> createInstrumentsFuture(
            final MarketInstrument... instruments
    ) {
        final List<MarketInstrument> instrumentList = List.of(instruments);
        final MarketInstrumentList marketInstrumentList = new MarketInstrumentList().instruments(instrumentList);
        return CompletableFuture.completedFuture(marketInstrumentList);
    }

    public static List<BigDecimal> getBigDecimalValues(final List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<Optional<BigDecimal>> getOptionalBigDecimalValues(final List<Double> values) {
        return values.stream()
                .map(DecimalUtils::setDefaultScale)
                .map(Optional::of)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static MockedStatic<Runtime> mockRuntime(final Runtime runtime) {
        final MockedStatic<Runtime> runtimeStaticMock = Mockito.mockStatic(Runtime.class, Mockito.CALLS_REAL_METHODS);
        runtimeStaticMock.when(Runtime::getRuntime).thenReturn(runtime);
        return runtimeStaticMock;
    }

    public static Point createPoint(final Candle candle) {
        return Point.of(candle.getTime(), candle.getOpenPrice());
    }

    public static List<BigDecimal> createBigDecimalsList(final Double... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createBigDecimalsList(final Integer... values) {
        return Stream.of(values).map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    public static List<BigDecimal> createRandomBigDecimalsList(final int size) {
        final Random random = new Random();
        final List<BigDecimal> values = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            values.add(DecimalUtils.setDefaultScale(random.nextDouble()));
        }
        return values;
    }

    @SneakyThrows
    public static CronExpression createCronExpression() {
        return new CronExpression("0 * * * * ?");
    }

}