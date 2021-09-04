package ru.obukhov.trader.test.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.config.properties.TradingProperties;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.obukhov.trader.trading.model.DecisionData;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrumentList;
import ru.tinkoff.invest.openapi.model.rest.MoneyAmount;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class TestData {

    // region Tinkoff Candle creation

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
        return createTinkoffCandle(interval, openPrice, closePrice, highestPrice, lowestPrice, OffsetDateTime.now());
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

    // endregion

    // region Candle creation

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
        return new Candle().setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
    }

    public static Candle createCandleWithClosePrice(final double closePrice) {
        final Candle candle = new Candle();
        candle.setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
        return candle;
    }

    public static Candle createCandleWithOpenPriceAndClosePrice(final double openPrice, final double closePrice) {
        final Candle candle = new Candle();
        candle.setOpenPrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(openPrice)));
        candle.setClosePrice(DecimalUtils.setDefaultScale(BigDecimal.valueOf(closePrice)));
        return candle;
    }

    // endregion

    // region DecisionData creation

    public static DecisionData createDecisionData(final Candle... candles) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setCurrentCandles(List.of(candles));
        return decisionData;
    }

    public static DecisionData createDecisionData(
            final double averagePositionPrice,
            final int positionLotsCount,
            final int lotSize,
            final double currentPrice
    ) {
        final DecisionData decisionData = new DecisionData();
        decisionData.setPosition(createPortfolioPosition(averagePositionPrice, positionLotsCount));
        decisionData.setCurrentCandles(List.of(createCandleWithOpenPrice(currentPrice)));
        decisionData.setInstrument(createMarketInstrument(lotSize));

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

    // endregion

    // region PortfolioPosition creation

    public static PortfolioPosition createPortfolioPosition(final String ticker) {
        return createPortfolioPosition(ticker, 1);
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

    // endregion

    // region CurrencyPosition creation

    public static CurrencyPosition createCurrencyPosition(final Currency currency, final long balance) {
        return new CurrencyPosition()
                .currency(currency)
                .balance(DecimalUtils.setDefaultScale(balance));
    }

    public static CurrencyPosition createCurrencyPosition(final Currency currency, final long balance, final long blocked) {
        return new CurrencyPosition()
                .currency(currency)
                .balance(DecimalUtils.setDefaultScale(balance))
                .blocked(DecimalUtils.setDefaultScale(blocked));
    }

    // endregion

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

    public static MoneyAmount createMoneyAmount(final Currency currency, final long value) {
        return new MoneyAmount()
                .currency(currency)
                .value(BigDecimal.valueOf(value));
    }

    // region MarketInstrument creation

    private static MarketInstrument createMarketInstrument(int lotSize) {
        final MarketInstrument instrument = new MarketInstrument();
        instrument.setLot(lotSize);
        return instrument;
    }


    public static CompletableFuture<MarketInstrumentList> createInstrumentsFuture(final MarketInstrument... instruments) {
        final List<MarketInstrument> instrumentList = List.of(instruments);
        final MarketInstrumentList marketInstrumentList = new MarketInstrumentList().instruments(instrumentList);
        return CompletableFuture.completedFuture(marketInstrumentList);
    }

    // endregion

    // region BigDecimals list creation

    public static List<BigDecimal> createBigDecimalsList(final List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
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

    // endregion

    @SneakyThrows
    public static CronExpression createCronExpression() {
        return new CronExpression("0 * * * * ?");
    }

    public static TradingProperties createTradingProperties() {
        return new TradingProperties(
                false,
                "token",
                0.003,
                DateTimeTestData.createTime(10, 0, 0),
                Duration.ofHours(9),
                7,
                OffsetDateTime.now()
        );
    }

}